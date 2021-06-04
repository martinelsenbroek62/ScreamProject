#!/bin/bash
docker cp sql/create-db.sql skyscraper-db:/var/create-db.sql
docker exec -it --user=oracle skyscraper-db bash -c "exit | sqlplus sys/Topcoder123@XE as sysdba @/var/create-db.sql"
