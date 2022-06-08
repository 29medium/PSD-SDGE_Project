#!/bin/bash

erl -pa dependencies/default/lib/chumak/ebin -pa Collector -eval "tcpHandler:run($@)"