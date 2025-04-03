#!/bin/sh
mc alias set myminio http://localhost:9000 tms-user tms-password

# Check if the bucket exists before trying to create it
if ! mc ls myminio/loki-data > /dev/null 2>&1; then
    mc mb myminio/loki-data
    echo "Bucket 'loki-data' created."
else
    echo "Bucket 'loki-data' already exists."
fi
