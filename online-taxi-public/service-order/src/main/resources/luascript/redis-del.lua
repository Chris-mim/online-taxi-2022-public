local key = KEYS[1];
local value = KEYS[1];

if redis.call("get", key) == value then
    redis.call("del", key)
    return true;
else
    return false;
end