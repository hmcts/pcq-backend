#!/usr/bin/env bash
echo ${TEST_URL}
echo "Debug 0"
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -S -d -u ${SecurityRules} -P 1001 -l FAIL
cat zap.out
echo "ZAP has successfully started"
curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
echo "Debug 1"
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.html -f html
echo "Debug 2"
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l Informational --exit-code False
echo "Debug 3"
mkdir -p functional-output
chmod a+wx functional-output
cp /zap/api-report.html functional-output/
