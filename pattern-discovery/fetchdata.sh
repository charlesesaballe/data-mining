#!/bin/bash

if ! [ -s "./data/reuters21578" ]
then
  echo "[INFO]: downloading reuters21578 dataset..."
  mkdir -p data/reuters21578
  wget -O ./data/reuters21578.tar.gz http://www.daviddlewis.com/resources/testcollections/reuters21578/reuters21578.tar.gz
  tar -C ./data/reuters21578 -xvf ./data/reuters21578.tar.gz
  echo "[INFO]: DONE downloading and unpacking reuters21578 dataset."
else
  echo "[WARN]: reuters21578 dataset seems to be present already."
fi