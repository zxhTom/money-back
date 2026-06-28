#!/usr/bin/env python3
"""
暴力破解告警测试脚本
用法：python3 brute_force_test.py [选项]

原理：SecurityDetectFilter 在验证码校验之前计数，
每次请求到达 /system/auth/login 就会累加 Redis 计数器。
登录返回"验证码不能为空"属于正常结果（计数已累加），
超过阈值后 Filter 直接返回 403 并触发告警。
"""

import argparse
import json
import time
import urllib.request
import urllib.error

DEFAULT_URL      = "https://brain.zxhtom.store/admin-api/system/auth/login"
DEFAULT_USER     = "admin"
DEFAULT_COUNT    = 20    # 需超过告警规则阈值
DEFAULT_INTERVAL = 0.3

FAKE_PASSWORDS = [
    "123456", "password", "admin123", "111111", "888888",
    "qwerty", "abc123", "letmein", "monkey", "dragon",
    "master", "123123", "654321", "000000", "pass1234",
    "admin@123", "P@ssw0rd", "test1234", "root123", "guest",
]


def do_login(url: str, username: str, password: str, timeout: int = 8) -> tuple:
    payload = json.dumps({"username": username, "password": password}).encode()
    req = urllib.request.Request(
        url,
        data=payload,
        headers={
            "Content-Type": "application/json",
            "User-Agent": "Mozilla/5.0 (BruteForce-Test/1.0)",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode())
        except Exception:
            return e.code, {"msg": str(e)}
    except Exception as e:
        return -1, {"msg": str(e)}


def classify(http_status, body):
    """返回状态标签"""
    if http_status == 403:
        return "BLOCKED"       # Filter 已封禁 —— 告警已触发
    msg = body.get("msg", "")
    if "验证码" in msg:
        return "COUNTED"       # 计数已累加，等待阈值
    if "密码" in msg or "用户" in msg or "账号" in msg:
        return "AUTH_FAIL"     # 验证码已关闭或通过，密码错
    if http_status == 200 and body.get("code") == 0:
        return "SUCCESS"
    if http_status == -1:
        return "NET_ERR"
    return "HTTP{}".format(http_status)


def run(url, username, count, interval):
    print("目标   : {}".format(url))
    print("用户名 : {}".format(username))
    print("次数   : {}  间隔: {}s".format(count, interval))
    print("说明   : COUNTED=计数累加中  BLOCKED=Filter已封禁(告警触发)")
    print("-" * 70)

    stats = {"COUNTED": 0, "BLOCKED": 0, "AUTH_FAIL": 0, "SUCCESS": 0, "OTHER": 0}

    for i in range(count):
        pwd = FAKE_PASSWORDS[i % len(FAKE_PASSWORDS)]
        http_status, body = do_login(url, username, pwd)
        label = classify(http_status, body)

        key = label if label in stats else "OTHER"
        stats[key] += 1

        msg = (body.get("msg") or "")[:55]
        print("[{:>3}/{}] {:<10}  pwd={:<12}  {}".format(i + 1, count, label, pwd, msg))

        if label == "BLOCKED":
            print("\n>>> Filter 已封禁，告警已触发！继续发请求验证封禁持续有效...")
            for j in range(i + 1, count):
                pwd2 = FAKE_PASSWORDS[j % len(FAKE_PASSWORDS)]
                hs, bd = do_login(url, username, pwd2)
                lbl = classify(hs, bd)
                stats["BLOCKED"] += 1
                print("[{:>3}/{}] {:<10}  pwd={:<12}  {}".format(
                    j + 1, count, lbl, pwd2, (bd.get("msg") or "")[:55]))
                if interval > 0:
                    time.sleep(interval)
            break

        if i < count - 1:
            time.sleep(interval)

    print("-" * 70)
    print("汇总：COUNTED={}  BLOCKED={}  AUTH_FAIL={}".format(
        stats["COUNTED"], stats["BLOCKED"], stats["AUTH_FAIL"]))

    if stats["BLOCKED"] > 0:
        print("✓ 告警已触发，请到后台「安全告警」页确认记录。")
    else:
        print("! 未触发封禁，当前累计 {} 次请求。若未触发请检查：".format(count))
        print("  1. 告警规则阈值是否大于本次请求次数")
        print("  2. 规则是否已启用")
        print("  3. Redis 计数器是否在测试前就已过期（超出窗口期）")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--url",      default=DEFAULT_URL)
    parser.add_argument("--user",     default=DEFAULT_USER)
    parser.add_argument("--count",    default=DEFAULT_COUNT, type=int)
    parser.add_argument("--interval", default=DEFAULT_INTERVAL, type=float)
    args = parser.parse_args()
    run(args.url, args.user, args.count, args.interval)
