#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo Starting agent...
echo Running $DIR/utils/fetcher/fetchers.py
python $DIR/utils/fetcher/fetchers.py $DIR/config.ini
