#!/bin/sh
# Updates the specified html files with template code

processHTML()
{
  FILENAME=$1
  BASENAME=$(echo $FILENAME | sed 's/[^a-z0-9]*\([a-z_]*\)\.html/\1/g')

  echo "Processing $FILENAME"

  rm -rf ./tmp
  mkdir ./tmp

  #sed -e "s/{{{page_name}}}/$BASENAME/g" -e "s/{{{min}}}/$MIN/g" head.txt > ./tmp/head.txt
  #sed -e "s/{{{page_name}}}/$BASENAME/g" -e "s/{{{min}}}/$MIN/g" script.txt > ./tmp/script.txt
  
  sed -f head_delete.sed $FILENAME | sed -f head_insert.sed > ./tmp/$BASENAME.1
  sed -f script_delete.sed ./tmp/$BASENAME.1 | sed -f script_insert.sed > ./tmp/$BASENAME.2
  sed -f header_delete.sed ./tmp/$BASENAME.2 | sed -f header_insert.sed > ./tmp/$BASENAME.3
  sed -f footer_delete.sed ./tmp/$BASENAME.3 | sed -f footer_insert.sed > ./tmp/$BASENAME.4

  cp -f ./tmp/$BASENAME.4 $FILENAME
  rm -rf ./tmp  
}

processHTML ../login.html
processHTML ../index.html
processHTML ../stream.html
processHTML ../search.html
processHTML ../admin_users.html
processHTML ../admin_repos.html
processHTML ../my_account.html

