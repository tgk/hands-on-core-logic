#!/usr/bin/env bash

ps aux | grep lein | grep nrepl-uri | awk '{print $2}' | xargs kill
