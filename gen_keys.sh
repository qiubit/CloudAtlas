#!/bin/bash
openssl genrsa -out private_key.pem 1024
openssl rsa -in private_key.pem -pubout -outform DER -out public.der
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private.der -nocrypt