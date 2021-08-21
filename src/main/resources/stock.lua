--- 减库存，
--- 如果 （存在 keys[1] 并且 get(keys[1]) > 1）, decr(keys[1], -1)
--- 否则 return 0
if (redis.call('exists', KEYS[1]) == 1) then
    local stock = tonumber(redis.call('get', KEYS[1])); -- tonumber() str -> number
    if (stock > 0) then
        redis.call('incrby', KEYS[1], -1);
        return stock;
    end;
    return 0;
end;
