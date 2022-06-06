#!/bin/bash

erl -pa ../dependencies/default/lib/chumak/ebin -eval "tcpHandler:run(1199)"