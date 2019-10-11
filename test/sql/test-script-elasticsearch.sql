curl -XPUT "http://192.168.56.101:9200/t_account"
curl -XPUT "http://192.168.56.101:9200/t_address"

curl -XPUT "http://192.168.56.101:9200/t_account/_doc/1" -H 'Content-Type: application/json' -d '{"name": "a","age" : "1"}'
curl -XPUT "http://192.168.56.101:9200/t_account/_doc/2" -H 'Content-Type: application/json' -d '{"name": "b","age" : "2"}'
curl -XPUT "http://192.168.56.101:9200/t_account/_doc/3" -H 'Content-Type: application/json' -d '{"name": "c","age" : "3"}'
curl -XPUT "http://192.168.56.101:9200/t_account/_doc/4" -H 'Content-Type: application/json' -d '{"name": "d","age" : "4"}'
curl -XPUT "http://192.168.56.101:9200/t_account/_doc/5" -H 'Content-Type: application/json' -d '{"name": "e","age" : "5"}'
curl -XPUT "http://192.168.56.101:9200/t_account/_doc/6" -H 'Content-Type: application/json' -d '{"name": "f","age" : "6"}'
curl -XPUT "http://192.168.56.101:9200/t_account/_doc/7" -H 'Content-Type: application/json' -d '{"name": "g","age" : "7"}'
curl -XPUT "http://192.168.56.101:9200/t_account/_doc/8" -H 'Content-Type: application/json' -d '{"name": "h","age" : "8"}'
curl -XPUT "http://192.168.56.101:9200/t_account/_doc/9" -H 'Content-Type: application/json' -d '{"name": "i","age" : "9"}'

curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/1' -H 'Content-Type: application/json' -d '{"name": "a","home" : "1", "work" : "w1"}'
curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/2' -H 'Content-Type: application/json' -d '{"name": "b","home" : "2", "work" : "w2"}'
curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/3' -H 'Content-Type: application/json' -d '{"name": "c","home" : "3", "work" : "w3"}'
curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/4' -H 'Content-Type: application/json' -d '{"name": "d","home" : "4", "work" : "w4"}'
curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/5' -H 'Content-Type: application/json' -d '{"name": "e","home" : "5", "work" : "w5"}'
curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/6' -H 'Content-Type: application/json' -d '{"name": "f","home" : "6", "work" : "w6"}'
curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/7' -H 'Content-Type: application/json' -d '{"name": "g","home" : "7", "work" : "w7"}'
curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/8' -H 'Content-Type: application/json' -d '{"name": "h","home" : "8", "work" : "w8"}'
curl -XPUT 'http://192.168.56.101:9200/t_address/_doc/9' -H 'Content-Type: application/json' -d '{"name": "i","home" : "9", "work" : "w9"}'
