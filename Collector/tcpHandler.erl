-module(tcpHandler).
-export([run/1]).

run(Port) -> 
	% inicialização do socket push ZeroMQ
	application:start(chumak),
	{ok,PushSocket} = chumak:socket(push),
	case chumak:connect(PushSocket,tcp,"localhost",Port+1) of
		{ok, _BindPid} ->
			io:format("Binding OK, with Pid ~p\n",[_BindPid]);
		{error, Reason}->
			io:format("Connection failed: ~p\n",[Reason]);
		X ->
			io:format("Undhandled reply for bind ~p\n",[X])
	end,

	% inicialização do socket TCP
    {ok, LSock} = gen_tcp:listen(Port, [{active, once}, {packet, line},
                                      {reuseaddr, true}]),
	
	% inicialização do loginManager
	loginManager:run(),
	% Get to PID do loginManager
	LoginManager = whereis(loginManager),
	io:format("pid: ~p\n",[LoginManager]),

	% inicialização do acceptor de conexões TCP
	spawn(fun() -> acceptor(LSock,LoginManager, PushSocket) end),
  	
	% manter o processo vivo para evitar que socket quebre
	receive
		  after infinity ->
			  ok
	end.

% aceitador de conexões TCP
acceptor(LSock,LoginManager, PushSocket) ->
  {Result, Sock} = gen_tcp:accept(LSock),
  case Result of
	ok ->
		% Em caso de OK, cria um novo processo que vai tomar conta das conexões TCP
		io:format("Sucesso: ~p\n",[Result]),
  		spawn(fun() -> acceptor(LSock,LoginManager, PushSocket) end),
		% chamada para função que trata de um device acabado de conectar (não está autenticado)
  		userHandler(Sock,LoginManager, PushSocket);
	_ -> 
		io:format("Erro: ~p\n",[Result]),
		acceptor(LSock,LoginManager,PushSocket)
	end.

% função que trata da autenticação de um device
userHandler(Sock,LoginManager, PushSocket) ->
	receive
		{_, created} ->
			% mensagem recebida do Login Manager quando uma conta é criada
			inet:setopts(Sock, [{active, once}]),
			%gen_tcp:send(Sock,"Conta criada com sucesso\n"),
			io:format("Conta criada com sucesso\n"),
			userHandler(Sock, LoginManager, PushSocket);
		{_,user_exists} -> 
		    % mensagem recebida do Login Manager após uma tentativa de criação de conta quando o device já existe
			inet:setopts(Sock, [{active, once}]),
			%gen_tcp:send(Sock,"User já existe com esse username\n"),
			io:format("User já existe com esse username\n"),
			userHandler(Sock, LoginManager, PushSocket);
		{_ ,logged, User, Type} ->
			% mensagem recebida do Login Manager quando a autenticação é bem sucedida
			inet:setopts(Sock, [{active, once}]),
			gen_tcp:send(Sock,"Success\n"),
			io:format("Logged in\n"),
			% envio de mensagem para o Agregador, dizendo que um device ficou online
			ok = chumak:send(PushSocket,"login,"++User++","++Type),
			% chamada do handler de um device autenticado
			autenticado(Sock,LoginManager,User, PushSocket);
		{_ ,invalid} ->
			% mensagem recebida do Login Manager quando a pass enviada é inválida
			inet:setopts(Sock, [{active, once}]),
			io:format("Invalid\n"),
			userHandler(Sock, LoginManager, PushSocket);
		{_,alreadyOnline} ->
			% mensagem recebida do Login Manager quando o device já se encontra online
			inet:setopts(Sock, [{active, once}]),
			io:format("Already online\n"),
			userHandler(Sock, LoginManager, PushSocket);
		{tcp, _, "register " ++ Data} ->
			% match de quando o device se quer registar
			% divisão de argumentos pelos espaços
			Args = string:tokens(Data, [$\s]),
			io:format("Register Args: ~p\n",[Args]),
			% caso o número de argumentos seja diferente de 3, o registo é invalido
			case mylength(Args) of 
				3 ->
					User = lists:nth(1,Args),
					Pass1 = lists:nth(2,Args),
					% retirar o '\n' do final com o trim
					Pass2 = string:trim(lists:nth(3,Args)),
					% verificação de pass's coincidirem
					case string:equal(Pass1,Pass2) of 
						true ->
							LoginManager ! {create_account,User, Pass1, self()};
						_ ->
							%gen_tcp:send(Sock,"Palavras pass não coincidem\n"),
							io:format("Palavras pass não coincidem\n")
					end,
					inet:setopts(Sock, [{active, once}]),
					userHandler(Sock,LoginManager, PushSocket);
				_ -> 
					inet:setopts(Sock, [{active, once}]),
					io:format("bad arguments\n"),
					
					userHandler(Sock,LoginManager, PushSocket)
			end;
		{tcp, _, "login " ++ Data} ->
			% match de quando o device quer dar login
			% divisão de argumentos pelos espaços
			Args = string:tokens(Data, [$\s]),
			% caso o número de argumentos seja diferente de 3, o registo é invalido
			case mylength(Args) of 
				3 ->
					User = lists:nth(1,Args),
					Pass = lists:nth(2,Args),
					Type = string:trim(lists:nth(3,Args)),
					% envio de mensagem para o processo LoginManager para pedido de login de utilizador
					LoginManager ! {login, User, Pass, Type,self()},
					inet:setopts(Sock, [{active, once}]),
					userHandler(Sock,LoginManager, PushSocket);
				_ -> 
					inet:setopts(Sock, [{active, once}]),
					io:format("bad arguments\n"),
					userHandler(Sock,LoginManager, PushSocket)
			end;
		{tcp_closed, _} ->
			% Quando o socket TCP fecha, o processo termina também
			io:format("tcp closed\n");
		{tcp_error, _, _} ->
			% Quando o socket TCP termina com erro, o processo termina também
			io:format("tcp error\n")
  	end.

% handler do envio de eventos de um device já autenticado
% também verifica quando um utilizador se torna inativo
autenticado(Sock,LoginManager,User, PushSocket) ->
	receive
		{tcp,_,"event " ++ Data} ->
			% match de quando o device envia um evento
			inet:setopts(Sock, [{active, once}]),
			%gen_tcp:send(Sock,"Evento registado\n"),
			io:format("~p : Evento! ~p\n",[User,Data]),
			% envio do evento para o agregador
			ok = chumak:send(PushSocket,"event," ++ User ++","++string:trim(Data)),
			autenticado(Sock,LoginManager,User, PushSocket);
		{tcp, _, "logout\n"} ->
			% match de quando o device quer dar logout
			LoginManager ! {logout,User,self()},
			inet:setopts(Sock, [{active, once}]),
			% Envio de mensagem avisando agregador que device se desconectou
			ok = chumak:send(PushSocket,"logout,"++User),
			%gen_tcp:send(Sock,"Logged out...\n"),
			autenticado(Sock,LoginManager,User, PushSocket);
		{tcp_closed, _} ->
			% Quando o socket TCP fecha, o processo termina também
			io:format("tcp closed ... logging out (ativo)\n"),
			% Envio de mensagem avisando agregador que device se desconectou
			ok = chumak:send(PushSocket,"logout,"++User),
			LoginManager ! {logout, User,self()};
		{tcp_error, _, _} ->
			% Quando o socket TCP termina com erro, o processo termina também
			io:format("tcp error ... logging out (ativo)\n"),
			% Envio de mensagem avisando agregador que device se desconectou
			ok = chumak:send(PushSocket,"logout,"++User),
			LoginManager ! {logout, User,self()}
		after 5000 -> 
			% Tempo de inatividade de um device
			% quando entra aqui, é sinal que device ficou inativo
			io:format("Inativo ~p\n",[User]),
			% Envio de mensagem avisando agregador que device se encontra inativo
			ok = chumak:send(PushSocket,"inactive,"++User),
			% Chamada para handler de utilizadores inativos
			inactive(Sock,LoginManager,User,PushSocket)
	end.

% handler de utilizadores inativos
% ao receber um evento, muda utilizador para ativo
% pode receber um logout, event, ou o socket fechar/quebrar
inactive(Sock,LoginManager,User, PushSocket) ->
	receive
		{tcp,_,"event " ++ Data} ->
			% match de quando o device envia um evento
			inet:setopts(Sock, [{active, once}]),
			%gen_tcp:send(Sock,"Evento registado\n"),
			io:format("~p : Evento! ~p\n",[User,Data]),
			% Envio de evento para o agregador
			ok = chumak:send(PushSocket,"event," ++ User ++","++string:trim(Data)),
			% mudança de utilizador para ativo
			autenticado(Sock,LoginManager,User, PushSocket);
		{tcp, _, "logout\n"} ->
			% match de quando o device quer dar logout
			LoginManager ! {logout,User,self()},
			inet:setopts(Sock, [{active, once}]),
			% Envio de mensagem avisando agregador que device se desconectou
			ok = chumak:send(PushSocket,"logout,"++User),
			%gen_tcp:send(Sock,"Logged out...\n"),
			autenticado(Sock,LoginManager,User, PushSocket);
		{tcp_closed, _} ->
			% Quando o socket TCP fecha, o processo termina também
			io:format("tcp closed ... logging out (inativo\n"),
			% Envio de mensagem avisando agregador que device se desconectou
			ok = chumak:send(PushSocket,"logout,"++User),
			LoginManager ! {logout, User,self()};
		{tcp_error, _, _} ->
			% Quando o socket TCP termina com erro, o processo termina também
			io:format("tcp error ... logging out (inativo)\n"),
			% Envio de mensagem avisando agregador que device se desconectou
			ok = chumak:send(PushSocket,"logout,"++User),
			LoginManager ! {logout, User,self()}
	end.
	
% funcão que devolve tamanho de uma lista
mylength([]) -> 0;
mylength([_|T]) -> 1+mylength(T).
