#!/bin/bash

LOG_FILE="logs/user-management-security.log"

echo "=== Security Log Analysis ==="
echo ""

echo "Failed Login Attempts:"
grep "Failed login" $LOG_FILE | wc -l

echo ""
echo "Top IPs with Failed Logins:"
grep "Failed login" $LOG_FILE | grep -oP 'IP: \K[0-9.]+' | sort | uniq -c | sort -nr | head -5

echo ""
echo "Successful Logins Today:"
grep "$(date +%Y-%m-%d)" $LOG_FILE | grep "Successful login" | wc -l

echo ""
echo "User Deletions Today:"
grep "$(date +%Y-%m-%d)" $LOG_FILE | grep "User deletion" | wc -l

echo ""
echo "Unauthorized Access Attempts:"
grep "Unauthorized access" $LOG_FILE | wc -l