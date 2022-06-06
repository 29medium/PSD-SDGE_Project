-module(loginManager).
-export([run/0, create_account/2, close_account/2, login/2, logout/1]).

run() -> 
	register(?MODULE, spawn ( fun() -> loginManager(#{}) end) ).

create_account(User, Pass) ->
	?MODULE ! {create_account, User, Pass, self()},
	receiveReply().

close_account(User, Pass) ->
	?MODULE ! {close_account, User, Pass, self()},
	receiveReply().

login(User, Pass) ->
	?MODULE ! {login, User, Pass, self()},
	receiveReply().

logout(User) ->
	?MODULE ! {logout, User, self()},
	receiveReply().

loginManager(Map) ->
  	receive
    	{create_account, User, Pass, From} ->
      		case maps:find(User, Map) of
        		error ->
          			From ! {?MODULE, created},
					io:format("New user ~p\n", [User]),
          			loginManager(maps:put(User, {Pass, false}, Map));
        		_ -> 
          			From ! {?MODULE, user_exists},
          			loginManager(Map)
      		end;

    	{close_account, User, Pass, From} ->
      		case maps:find(User, Map) of
        		{ok, {Pass, _}} ->
          			From ! {?MODULE, ok},
          			loginManager(maps:remove(User, Map));
        		_ -> 
          			From ! {?MODULE, invalid},
          			loginManager(Map)
      		end;

    	{login, User, Pass, Type,From} ->
      		case maps:find(User, Map) of
        		{ok, {Pass, false}} ->
          			From ! {?MODULE, logged, User,Type},
          			loginManager(maps:update(User, {Pass, true}, Map));
				{ok, {Pass, true}} ->
					From ! {?MODULE,alreadyOnline},
					loginManager(Map);
        		_ -> 
          			From ! {?MODULE, logged,User,Type},
					io:format("New user ~p\n", [User]),
          			loginManager(maps:put(User, {Pass, true}, Map))
					%From ! {?MODULE, invalid},
          			%loginManager(Map)
      		end;

    	{logout, User, From} -> 
      		From ! {?MODULE, ok},
      		{Pass, _} = maps:get(User, Map),
      		loginManager(maps:update(User, {Pass, false}, Map))
    end.

receiveReply() ->
	receive
		{?MODULE, Reply} -> 
			Reply
	end.