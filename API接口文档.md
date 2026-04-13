# 直播平台 API 接口文档

## 文档信息

- **版本**: v1.0.0
- **更新日期**: 2026-02-25
- **接口前缀**: `/api`
- **数据格式**: JSON

---

## 通用说明

### 接口响应格式

所有接口返回统一格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 禁止访问（权限不足） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 认证方式

使用 JWT Token 认证，在请求头中添加：

```
Authorization: Bearer {token}
```

---

## 用户模块

### 1. 用户注册

**接口**: `POST /api/user/register`

**请求体**:
```json
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "userType": 0,
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 2. 用户登录

**接口**: `POST /api/user/login`

**请求体**:
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "userType": 0,
      "avatar": "http://..."
    }
  }
}
```

### 3. 发送短信验证码

**接口**: `POST /api/user/send-sms-code`

**请求体**:
```json
{
  "phone": "13800138000"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": null
}
```

### 4. 手机号验证码登录

**接口**: `POST /api/user/login-with-phone`

**请求体**:
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

**响应**: 同用户登录响应

### 5. 第三方登录

**接口**: `POST /api/user/oauth2/{provider}`

**路径参数**:
- `provider`: 第三方平台 (`wechat`, `qq`, `weibo`)

**请求体**:
```json
{
  "code": "authorization_code_from_third_party"
}
```

**响应**: 同用户登录响应

### 6. 获取当前用户信息

**接口**: `GET /api/user/info`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com",
    "avatar": "http://...",
    "userType": 0,
    "realName": "张三",
    "idCard": "110101199001011234",
    "status": 1,
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 7. 更新用户信息

**接口**: `PUT /api/user/update`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "nickname": "新昵称",
  "avatar": "http://...",
  "phone": "13800138000",
  "email": "new@example.com"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "nickname": "新昵称",
    "avatar": "http://..."
  }
}
```

### 8. 重置密码

**接口**: `POST /api/user/reset-password`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "newPassword": "newpassword123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

### 9. 获取用户列表（管理员）

**接口**: `GET /api/user/list`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取用户列表成功",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "userType": 1,
      "status": 1
    }
  ]
}
```

---

## 直播模块

### 1. 创建直播

**接口**: `POST /api/live/create`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "title": "直播标题",
  "description": "直播描述",
  "cover": "http://...",
  "categoryId": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建直播成功",
  "data": {
    "id": 1,
    "userId": 1,
    "title": "直播标题",
    "description": "直播描述",
    "cover": "http://...",
    "categoryId": 1,
    "streamKey": "stream_abc123",
    "status": 0,
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 2. 开始直播

**接口**: `POST /api/live/start/{liveId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "开始直播成功",
  "data": true
}
```

### 3. 结束直播

**接口**: `POST /api/live/end/{liveId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "结束直播成功",
  "data": true
}
```

### 4. 获取直播列表

**接口**: `GET /api/live/list`

**查询参数**:
- `status` (可选): 直播状态 (0-准备中, 1-直播中, 2-已结束)

**响应**:
```json
{
  "code": 200,
  "message": "获取直播列表成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "title": "直播标题",
      "description": "直播描述",
      "cover": "http://...",
      "categoryId": 1,
      "streamKey": "stream_abc123",
      "status": 1,
      "viewCount": 100,
      "userNickname": "主播昵称",
      "userAvatar": "http://...",
      "startTime": "2026-02-25T10:00:00"
    }
  ]
}
```

### 5. 获取直播详情

**接口**: `GET /api/live/detail/{liveId}`

**响应**:
```json
{
  "code": 200,
  "message": "获取直播详情成功",
  "data": {
    "id": 1,
    "userId": 1,
    "title": "直播标题",
    "description": "直播描述",
    "cover": "http://...",
    "categoryId": 1,
    "streamKey": "stream_abc123",
    "status": 1,
    "viewCount": 100,
    "userNickname": "主播昵称",
    "userAvatar": "http://...",
    "startTime": "2026-02-25T10:00:00",
    "endTime": null
  }
}
```

### 6. 获取推流地址

**接口**: `GET /api/live/push-url/{liveId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取推流地址成功",
  "data": "rtmp://localhost:1935/live/stream_abc123"
}
```

### 7. 获取播放地址

**接口**: `GET /api/live/play-url/{liveId}`

**响应**:
```json
{
  "code": 200,
  "message": "获取播放地址成功",
  "data": "http://localhost:8080/hls/stream_abc123.m3u8"
}
```

### 8. 删除直播

**接口**: `DELETE /api/live/delete/{liveId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "删除直播成功",
  "data": true
}
```

### 9. 获取分类列表

**接口**: `GET /api/live/category/list`

**响应**:
```json
{
  "code": 200,
  "message": "获取分类列表成功",
  "data": [
    {
      "id": 1,
      "name": "游戏",
      "description": "游戏直播",
      "sort": 1
    },
    {
      "id": 2,
      "name": "娱乐",
      "description": "娱乐直播",
      "sort": 2
    }
  ]
}
```

---

## 弹幕模块

### 1. 发送弹幕

**接口**: `POST /api/danmaku/send`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "liveId": 1,
  "content": "弹幕内容",
  "color": "#FFFFFF"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "id": 1,
    "liveId": 1,
    "userId": 1,
    "content": "弹幕内容",
    "color": "#FFFFFF",
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 2. 获取弹幕列表

**接口**: `GET /api/danmaku/list/{liveId}`

**响应**:
```json
{
  "code": 200,
  "message": "获取弹幕列表成功",
  "data": [
    {
      "id": 1,
      "liveId": 1,
      "userId": 1,
      "username": "用户1",
      "content": "弹幕内容",
      "color": "#FFFFFF",
      "createdAt": "2026-02-25T10:00:00"
    }
  ]
}
```

---

## 礼物模块

### 1. 获取礼物列表

**接口**: `GET /api/gift/list`

**响应**:
```json
{
  "code": 200,
  "message": "获取礼物列表成功",
  "data": [
    {
      "id": 1,
      "name": "火箭",
      "price": 100,
      "icon": "http://...",
      "animation": "rocket",
      "status": 1
    },
    {
      "id": 2,
      "name": "飞机",
      "price": 50,
      "icon": "http://...",
      "animation": "plane",
      "status": 1
    }
  ]
}
```

### 2. 赠送礼物

**接口**: `POST /api/gift/send`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "liveId": 1,
  "giftId": 1,
  "count": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "赠送成功",
  "data": {
    "id": 1,
    "liveId": 1,
    "userId": 1,
    "giftId": 1,
    "giftName": "火箭",
    "price": 100,
    "count": 1,
    "totalPrice": 100,
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 3. 获取礼物记录

**接口**: `GET /api/gift/records`

**请求头**:
```
Authorization: Bearer {token}
```

**查询参数**:
- `type` (可选): 类型 (`sent`-送出, `received`-收到)
- `page` (可选): 页码，默认1
- `size` (可选): 每页数量，默认20

**响应**:
```json
{
  "code": 200,
  "message": "获取礼物记录成功",
  "data": {
    "records": [
      {
        "id": 1,
        "liveId": 1,
        "giftName": "火箭",
        "price": 100,
        "count": 1,
        "totalPrice": 100,
        "createdAt": "2026-02-25T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20
  }
}
```

---

## 关注模块

### 1. 关注用户

**接口**: `POST /api/follow/{userId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "关注成功",
  "data": true
}
```

### 2. 取消关注

**接口**: `DELETE /api/follow/{userId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "取消关注成功",
  "data": true
}
```

### 3. 获取关注列表

**接口**: `GET /api/follow/following`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取关注列表成功",
  "data": [
    {
      "id": 2,
      "username": "user2",
      "nickname": "用户2",
      "avatar": "http://...",
      "followedAt": "2026-02-25T10:00:00"
    }
  ]
}
```

### 4. 获取粉丝列表

**接口**: `GET /api/follow/followers`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取粉丝列表成功",
  "data": [
    {
      "id": 3,
      "username": "user3",
      "nickname": "用户3",
      "avatar": "http://...",
      "followedAt": "2026-02-25T10:00:00"
    }
  ]
}
```

### 5. 检查是否关注

**接口**: `GET /api/follow/check/{userId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "检查成功",
  "data": true
}
```

---

## 评论模块

### 1. 添加评论

**接口**: `POST /api/comment/add`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "liveId": 1,
  "content": "评论内容",
  "parentId": null
}
```

**响应**:
```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": 1,
    "liveId": 1,
    "userId": 1,
    "content": "评论内容",
    "parentId": null,
    "likeCount": 0,
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 2. 获取评论列表

**接口**: `GET /api/comment/list/{liveId}`

**响应**:
```json
{
  "code": 200,
  "message": "获取评论列表成功",
  "data": [
    {
      "id": 1,
      "liveId": 1,
      "userId": 1,
      "username": "用户1",
      "avatar": "http://...",
      "content": "评论内容",
      "parentId": null,
      "likeCount": 10,
      "createdAt": "2026-02-25T10:00:00",
      "replies": []
    }
  ]
}
```

### 3. 点赞评论

**接口**: `POST /api/comment/like/{commentId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": true
}
```

### 4. 删除评论

**接口**: `DELETE /api/comment/delete/{commentId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
```

---

## 支付模块

### 1. 创建充值订单

**接口**: `POST /api/recharge/create`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "amount": 100
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建订单成功",
  "data": {
    "orderId": "R20260225100000",
    "amount": 100,
    "status": 0,
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 2. 获取充值记录

**接口**: `GET /api/recharge/records`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取充值记录成功",
  "data": [
    {
      "id": 1,
      "orderId": "R20260225100000",
      "amount": 100,
      "status": 1,
      "payTime": "2026-02-25T10:05:00",
      "createdAt": "2026-02-25T10:00:00"
    }
  ]
}
```

### 3. 申请提现

**接口**: `POST /api/withdraw/apply`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "amount": 100,
  "accountType": "alipay",
  "account": "13800138000",
  "realName": "张三"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "申请成功",
  "data": {
    "id": 1,
    "amount": 100,
    "status": 0,
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 4. 获取提现记录

**接口**: `GET /api/withdraw/records`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取提现记录成功",
  "data": [
    {
      "id": 1,
      "amount": 100,
      "status": 1,
      "accountType": "alipay",
      "account": "13800138000",
      "createdAt": "2026-02-25T10:00:00",
      "processedAt": "2026-02-25T12:00:00"
    }
  ]
}
```

### 5. 获取用户余额

**接口**: `GET /api/user/balance`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "balance": 1000.00,
    "totalIncome": 5000.00,
    "totalOutcome": 4000.00
  }
}
```

---

## 管理模块

### 1. 获取举报列表

**接口**: `GET /api/admin/reports`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取举报列表成功",
  "data": [
    {
      "id": 1,
      "type": 1,
      "targetId": 1,
      "reason": "违规内容",
      "status": 0,
      "reporterId": 2,
      "createdAt": "2026-02-25T10:00:00"
    }
  ]
}
```

### 2. 处理举报

**接口**: `POST /api/admin/report/handle`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "reportId": 1,
  "action": "ban_user",
  "remark": "确认违规"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "处理成功",
  "data": true
}
```

### 3. 获取敏感词列表

**接口**: `GET /api/admin/sensitive-words`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取敏感词列表成功",
  "data": [
    {
      "id": 1,
      "word": "敏感词",
      "level": 1,
      "createdAt": "2026-02-25T10:00:00"
    }
  ]
}
```

### 4. 添加敏感词

**接口**: `POST /api/admin/sensitive-word`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "word": "敏感词",
  "level": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "word": "敏感词",
    "level": 1
  }
}
```

---

## 数据分析模块

### 1. 获取数据总览

**接口**: `GET /api/analytics/overview`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalUsers": 10000,
    "totalLives": 500,
    "totalGifts": 100000,
    "todayActiveUsers": 1000,
    "todayLiveCount": 50,
    "todayGiftAmount": 5000.00
  }
}
```

### 2. 获取用户统计

**接口**: `GET /api/analytics/users`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "newUsersTrend": [
      {"date": "2026-02-20", "count": 100},
      {"date": "2026-02-21", "count": 120}
    ],
    "activeUsersTrend": [
      {"date": "2026-02-20", "count": 500},
      {"date": "2026-02-21", "count": 550}
    ]
  }
}
```

### 3. 获取直播统计

**接口**: `GET /api/analytics/lives`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "liveCountTrend": [
      {"date": "2026-02-20", "count": 10},
      {"date": "2026-02-21", "count": 12}
    ],
    "viewCountTrend": [
      {"date": "2026-02-20", "count": 1000},
      {"date": "2026-02-21", "count": 1200}
    ]
  }
}
```

### 4. 获取礼物统计

**接口**: `GET /api/analytics/gifts`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "giftAmountTrend": [
      {"date": "2026-02-20", "amount": 1000.00},
      {"date": "2026-02-21", "amount": 1200.00}
    ],
    "giftRanking": [
      {"giftName": "火箭", "count": 100, "amount": 10000.00},
      {"giftName": "飞机", "count": 200, "amount": 10000.00}
    ]
  }
}
```

---

## AI工具模块

### 1. 获取AI模型列表

**接口**: `GET /api/tools/image/models`

**查询参数**:
- `type` (可选): 模型类型

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "默认模型",
      "type": "default"
    },
    {
      "id": 2,
      "name": "艺术风格",
      "type": "art"
    }
  ]
}
```

### 2. 生成图片

**接口**: `POST /api/tools/image/generate`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "prompt": "一只可爱的猫咪",
  "modelId": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "id": 1,
    "prompt": "一只可爱的猫咪",
    "imageUrl": "http://...",
    "thumbnailUrl": "http://...",
    "createdAt": "2026-02-25T10:00:00"
  }
}
```

### 3. 获取生成历史

**接口**: `GET /api/tools/image/history`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "prompt": "一只可爱的猫咪",
      "imageUrl": "http://...",
      "thumbnailUrl": "http://...",
      "createdAt": "2026-02-25T10:00:00"
    }
  ]
}
```

---

## 观看历史模块

### 1. 获取观看历史

**接口**: `GET /api/watch-history/list`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取观看历史成功",
  "data": [
    {
      "id": 1,
      "liveId": 1,
      "liveTitle": "直播标题",
      "liveCover": "http://...",
      "anchorNickname": "主播昵称",
      "progress": 3600,
      "duration": 7200,
      "watchedAt": "2026-02-25T10:00:00"
    }
  ]
}
```

### 2. 清空观看历史

**接口**: `DELETE /api/watch-history/clear`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "清空成功",
  "data": true
}
```

---

## 通知模块

### 1. 获取通知设置

**接口**: `GET /api/notification/settings`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "liveStartNotify": true,
    "systemNotify": true,
    "emailNotify": false,
    "smsNotify": false
  }
}
```

### 2. 更新通知设置

**接口**: `PUT /api/notification/settings`

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "liveStartNotify": true,
  "systemNotify": true,
  "emailNotify": false,
  "smsNotify": false
}
```

**响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "liveStartNotify": true,
    "systemNotify": true,
    "emailNotify": false,
    "smsNotify": false
  }
}
```

---

## WebSocket 接口

### 连接地址

```
ws://localhost:8081/ws
```

### 订阅频道

#### 1. 弹幕频道

**订阅地址**:
```
/topic/danmaku/{liveId}
```

**消息格式**:
```json
{
  "id": 1,
  "liveId": 1,
  "userId": 1,
  "username": "用户1",
  "content": "弹幕内容",
  "color": "#FFFFFF",
  "createdAt": "2026-02-25T10:00:00"
}
```

#### 2. 礼物频道

**订阅地址**:
```
/topic/gift/{liveId}
```

**消息格式**:
```json
{
  "id": 1,
  "liveId": 1,
  "userId": 1,
  "username": "用户1",
  "giftId": 1,
  "giftName": "火箭",
  "count": 1,
  "animation": "rocket",
  "createdAt": "2026-02-25T10:00:00"
}
```

#### 3. 观看人数频道

**订阅地址**:
```
/topic/viewers/{liveId}
```

**消息格式**:
```json
{
  "liveId": 1,
  "viewCount": 100
}
```

#### 4. 开播通知频道

**订阅地址**:
```
/user/queue/notifications
```

**消息格式**:
```json
{
  "type": "live_start",
  "title": "关注的主播开播了",
  "content": "主播昵称 开始直播：直播标题",
  "liveId": 1,
  "createdAt": "2026-02-25T10:00:00"
}
```

---

## 错误码说明

| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| 400 | 请求参数错误 | 检查请求参数 |
| 401 | 未授权 | 重新登录获取Token |
| 403 | 禁止访问 | 检查用户权限 |
| 404 | 资源不存在 | 检查资源ID |
| 500 | 服务器内部错误 | 联系管理员 |
| 1001 | 用户名已存在 | 更换用户名 |
| 1002 | 手机号已注册 | 使用其他手机号 |
| 1003 | 验证码错误 | 重新输入验证码 |
| 1004 | 余额不足 | 充值后再试 |
| 1005 | 直播已结束 | 无法进行操作 |

---

