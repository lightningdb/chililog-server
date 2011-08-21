#!/bin/sh
# Updates the specified html files with template code

processHTML()
{
  FILENAME=$1
  BASENAME=$(echo $FILENAME | sed 's/[^a-z0-9]*\([a-z]*\)\.html/\1/g')

  echo "Processing $FILENAME"

  rm -rf ./tmp
  mkdir ./tmp

  #sed -e "s/{{{page_name}}}/$BASENAME/g" -e "s/{{{min}}}/$MIN/g" head.txt > ./tmp/head.txt
  #sed -e "s/{{{page_name}}}/$BASENAME/g" -e "s/{{{min}}}/$MIN/g" script.txt > ./tmp/script.txt
  
  sed -f head_delete.sed $FILENAME | sed -f head_insert.sed > ./tmp/$BASENAME.1
  sed -f script_delete.sed ./tmp/$BASENAME.1 | sed -f script_insert.sed > ./tmp/$BASENAME.2
  sed -f header_delete.sed ./tmp/$BASENAME.2 | sed -f header_insert.sed > ./tmp/$BASENAME.3
  sed -f divider_delete.sed ./tmp/$BASENAME.3 | sed -f divider_insert.sed > ./tmp/$BASENAME.4
  sed -f footer_delete.sed ./tmp/$BASENAME.4 | sed -f footer_insert.sed > ./tmp/$BASENAME.5

  cp -f ./tmp/$BASENAME.5 $FILENAME
  rm -rf ./tmp  
}

processHTML ../login.html
processHTML ../index.html
processHTML ../stream.html

