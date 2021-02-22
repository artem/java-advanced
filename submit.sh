#!/bin/bash

rsync -av --exclude='*Test.java' --exclude='*Tester.java' \
      --exclude='*.iml' --exclude='*module-info.java' \
      ./modules/info.kgeorgiy.ja.labazov/ \
      ../java-advanced/java-solutions

pushd ../java-advanced || exit

git add ./*
git commit -m "Sync homework [Automated commit]"
git push origin master

popd || exit
