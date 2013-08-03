#!/usr/bin/env bash

ps aux | grep thomas | grep lein | grep nrepl-uri | cut -d" " -f 10 | xargs kill
