%%%-------------------------------------------------------------------
%% @doc PSD-SDGE_Project public API
%% @end
%%%-------------------------------------------------------------------

-module(PSD-SDGE_Project_app).

-behaviour(application).

-export([start/2, stop/1]).

start(_StartType, _StartArgs) ->
    PSD-SDGE_Project_sup:start_link().

stop(_State) ->
    ok.

%% internal functions
