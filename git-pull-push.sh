#!/bin/bash

git pull
git pull git@github.com:denis-volnenko/maven-minio-wagon.git

git add .
git commit -m "Task Manager updated."

git push

git push git@github.com:denis-volnenko/maven-minio-wagon.git