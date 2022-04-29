#!/usr/bin/env bash
echo ${TEST_URL}
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -S -d -u ${SecurityRules} -P 1001 -l FAIL
echo "ZAP has successfully started"
curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
echo 'Zap scan has run but no report was generated.' > /zap/api-report.html && chmod a+wx /zap/api-report.html
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.html -f html
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l Informational --exit-code False
mkdir -p functional-output && chmod a+wx functional-output
cp /zap/api-report.html functional-output/
