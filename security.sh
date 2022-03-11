#!/usr/bin/env bash
echo ${TEST_URL}
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
echo "Debug 0"
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -S -d -u ${SecurityRules} -P 1001 -l FAIL
cat zap.out
echo "ZAP has successfully started"
curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
zap-cli --zap-url http://0.0.0.0 --port 1001 --verbose start
echo "Debug 1"
zap-cli --zap-url http://0.0.0.0 --port 1001 --verbose report -o /zap/zap-api-report.html -f html
echo "Debug 2"
zap-cli --zap-url http://0.0.0.0 --port 1001 --verbose alerts -l Informational --exit-code False
echo "Debug 3"
mkdir -p security-output
chmod a+wx security-output
cp /zap/zap-api-report.html security-output/
