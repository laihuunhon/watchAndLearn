var config = {
    "debug": true,
	  "dbService": "mongoDB",
    "token": {
        "expire": 31536000
    },
    'bcrypt': {
        'pass_salt_len': 10
    },
    "mail": {
        "transport": "SMTP", 
        "service": "Gmail",
        "from": "PhimCuaTui",
        "support": {
            "user": "PhimCuaTui <vnadv2012@gmail.com>",
            "email": "vnadv2012@gmail.com",
            "pass": "sar1tasa"
        }
    },
    baokim: {
        site_password: "ac9a68f2c347de02",
        api_username: "11316165esources",
        api_password: "11316165esources235shsd",
        merchant_id: "19196"
    },
    nganluong: {
        merchant_id: "39539",
        merchant_password: "loPA_z4etod!2",
        merchant_account: "laihuunhon@gmail.com",
        serviceUrl: "https://www.nganluong.vn/mobile_card.api.post.v2.php",
        func: "CardCharge",
        version: "2.0"
    },
    facebook: {
        appId: '1730953027137388',
        appSecret: '3a7512c3ccab8e822f6e8b228834859e'
    },
    cardService: "nganluong"
}
module.exports = config;