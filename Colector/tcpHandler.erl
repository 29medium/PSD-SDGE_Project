-module(tcpHandler).
-export([run/1, acceptor/2, userHandler/2]).

run(Port) -> 
    {ok, LSock} = gen_tcp:listen(Port, [{active, once}, {packet, line},
                                      {reuseaddr, true}]),
	loginManager:run(),
	LoginManager = whereis(loginManager),
	io:format("pid: ~p\n",[LoginManager]),
	acceptor(LSock,LoginManager).

acceptor(LSock,LoginManager) ->
  {Result, Sock} = gen_tcp:accept(LSock),
  case Result of
	ok ->
		io:format("Sucesso: ~p\n",[Result]),
  		spawn(fun() -> acceptor(LSock,LoginManager) end),
  		userHandler(Sock,LoginManager);
	_ -> 
		io:format("Erro: ~p\n",[Result]),
		userHandler(Sock,LoginManager)
	end.
userHandler(Sock,LoginManager) ->
	receive
		{_, created} ->
			inet:setopts(Sock, [{active, once}]),
			gen_tcp:send(Sock,"Conta criada com sucesso\n"),
			io:format("Conta criada com sucesso\n"),
			userHandler(Sock, LoginManager);
		{_,user_exists} -> 
			inet:setopts(Sock, [{active, once}]),
			gen_tcp:send(Sock,"User já existe com esse username\n"),
			io:format("User já existe com esse username\n"),
			userHandler(Sock, LoginManager);
		{_ ,logged, User} ->
			inet:setopts(Sock, [{active, once}]),
			gen_tcp:send(Sock,"Logged in\n"),
			io:format("Logged in\n"),
			autenticado(Sock,LoginManager,User);
		{_ ,invalid} ->
			inet:setopts(Sock, [{active, once}]),
			io:format("Invalid\n");
		{tcp, _, "register " ++ Data} ->
			Args = string:tokens(Data, [$\s]),
			io:format("Register Args: ~p\n",[Args]),
			case mylength(Args) of 
				3 ->
					User = lists:nth(1,Args),
					Pass1 = lists:nth(2,Args),
					Pass2 = string:trim(lists:nth(3,Args)),
					case string:equal(Pass1,Pass2) of 
						true ->
							LoginManager ! {create_account,User, Pass1, self()};
						_ ->
							gen_tcp:send(Sock,"Palavras pass não coincidem\n"),
							io:format("Palavras pass não coincidem\n")
					end,
					inet:setopts(Sock, [{active, once}]),
					userHandler(Sock,LoginManager);
				_ -> 
					inet:setopts(Sock, [{active, once}]),
					io:format("bad arguments\n"),
					
					userHandler(Sock,LoginManager)
			end;
		{tcp, _, "login " ++ Data} ->
			Args = string:tokens(Data, [$\s]),
			case mylength(Args) of 
				2 ->
					User = lists:nth(1,Args),
					Pass = string:trim(lists:nth(2,Args)),
					LoginManager ! {login, User, Pass, self()},
					inet:setopts(Sock, [{active, once}]),
					userHandler(Sock,LoginManager);
				_ -> 
					inet:setopts(Sock, [{active, once}]),
					io:format("bad arguments\n"),
					userHandler(Sock,LoginManager)
			end;
		{tcp_closed, _} ->
			LoginManager ! {logout, self()};
		{tcp_error, _, _} ->
			LoginManager ! {logout, self()}
  	end.

autenticado(Sock,LoginManager,User) ->
	receive
		{tcp,_,"event " ++ Data} ->
			inet:setopts(Sock, [{active, once}]),
			gen_tcp:send(Sock,"Evento registado\n"),
			io:format("Evento! ~p\n",[Data]),
			autenticado(Sock,LoginManager,User);
		{tcp, _, "logout"} ->
			LoginManager ! {logout,User,self()},
			inet:setopts(Sock, [{active, once}]),
			gen_tcp:send(Sock,"Evento registado\n"),
			autenticado(Sock,LoginManager,User)
	end.
	

mylength([]) -> 0;
mylength([_|T]) -> 1+mylength(T).
