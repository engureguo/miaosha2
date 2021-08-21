--- 如果 get(keys[1]) == argv[1] return del(keys[1])
--- 否则 return 0
if redis.call("get",KEYS[1])==ARGV[1] then
    return redis.call("del",KEYS[1])
else
    return 0
end
