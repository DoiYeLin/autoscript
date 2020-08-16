export const setSessionKey = (key, Value) => {
	window.sessionStorage.setItem(key, Value);
}
export const getSessionKey = (key, defaultValue) => {
	const item = window.sessionStorage.getItem(key);
	if (item == undefined && defaultValue != undefined) {
		return defaultValue
	}
	return item;
}

export const removeSessionKey = (key) => {
	window.sessionStorage.removeItem(key);
}

export const setLocalKey = (key, Value) => {
	window.localStorage.setItem(key, Value);
}

export const getLocalKey = (key, defaultValue) => {
	const item = window.localStorage.getItem(key);
	if (item == undefined && defaultValue != undefined) {
		return defaultValue
	}
	return item;
}
export const remLocalKey = (key) => {
	window.localStorage.removeItem(key);
}

export const reload = (arg) => {
	if(arg) {
		sessionStorage.setItem('reload', true);
	} else {
		let _reload = sessionStorage.getItem('reload');
		if(_reload == 'true') {
			sessionStorage.removeItem('reload');
			return true;
		}
	}
	return false;
}

export const strip = (num, precision = 12) => {
	return +parseFloat(num.toPrecision(precision));
}


export const randomString = (e) => {
	e = e || 32
	var t = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678'
	var a = t.length
	var n = ''
	for (var i = 0; i < e; i++) n += t.charAt(Math.floor(Math.random() * a))
	return n;
}

export const checkPhone = (phone) => {
	let reg = /^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}$/;
	return reg.test(phone);
}

export const formatPhone = (phone) => {
	return phone.replace(/\B(?=(?:\d{4})+$)/g, ' ');
}

export const checkMoney = (number) => {
	let reg = /^[0-9]+(.[0-9]{2})?$/;
	return reg.test(number)
}

export const checkNumber = (number) => {
	let reg = /^(-){0,1}[0-9]+([.]{1}[0-9]+){0,1}$/;
	return reg.test(number);
}

export const isInteger = (val) => {
	return val % 1 === 0;
}

export const getBaseUrl = (url) => {
    if (!url) {
        url = window.location.href;
    }
    var reg = /^((\w+):\/\/([^\/:]*)(?::(\d+))?)(.*)/;
    reg.exec(url);
    return RegExp.$1;
}

export const keyMirror = (obj) => {
	let key
	let mirrored = {}
	if (obj && typeof obj === 'object') {
		for (key in obj) {
			if ({}.hasOwnProperty.call(obj, key)) {
				mirrored[key] = key
			}
		}
	}
	return mirrored
}


// export const geolocation = () => {
// 	return new Promise((resolve, reject) => {
// 		// 定位返回值
// 		let geo_result = {
// 			geores: false,
// 			locationType: '',
// 			province: '定位失败'
// 		}
// 		let mapObj = new AMap.Map('gdmap');
// 		// 坐标，定位插件
// 		mapObj.plugin(['AMap.Geocoder', 'AMap.Geolocation'], function() {
// 			let geolocation = new AMap.Geolocation({
// 				enableHighAccuracy: true, //是否使用高精度定位，默认:true
// 				timeout: 10000 //超过5秒后停止定位，默认：无穷大
// 			});

// 			var geocoder = new AMap.Geocoder({
// 				// city 指定进行编码查询的城市，支持传入城市名、adcode 和 citycode
// 				city: '010'
// 			});

// 			mapObj.addControl(geolocation);
// 			geolocation.getCurrentPosition();
// 			//返回定位信息
// 			AMap.event.addListener(geolocation, 'complete', function(res) {
// 				// 定位成功, 坐标转换定位信息
// 				geocoder.getAddress(res.position, function(status, result) {
// 					if (status === 'complete' && result.info === 'OK') {
// 						// result为对应的地理位置详细信息
// 						geo_result = {
// 							geores: true,
// 							// geo 表示是高精度定位
// 							locationType: 'geo',
// 							// 高精度省份信息
// 							province: result.regeocode.addressComponent.province
// 						}
// 						resolve(geo_result);
// 					} else {
// 						console.log(result);
// 						resolve(geo_result);
// 					}
// 				});
// 			});

// 			//定位失败使用ip定位
// 			AMap.event.addListener(geolocation, 'error', function(err) {
// 				console.log(err);
// 				geolocation.getCityInfo((status, result) => {
// 					if (status === 'complete') {
// 						geo_result = {
// 							geores: true,
// 							// 表示ip定位
// 							locationType: 'ip',
// 							province: result.province
// 						}
// 						resolve(geo_result);
// 					}
// 				});
// 			});
// 		});
// 	});
// }


export const browser = {
	versions: function() {
		var u = navigator.userAgent,
			app = navigator.appVersion;
		return {
			trident: u.indexOf('Trident') > -1, //IE内核
			presto: u.indexOf('Presto') > -1, //opera内核
			webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
			gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
			mobile: !!u.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
			ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
			android: u.indexOf('Android') > -1 || u.indexOf('Adr') > -1, //android终端
			iPhone: u.indexOf('iPhone') > -1, //是否为iPhone或者QQHD浏览器
			iPad: u.indexOf('iPad') > -1, //是否iPad
			webApp: u.indexOf('Safari') == -1, //是否web应该程序，没有头部与底部
			weixin: u.indexOf('MicroMessenger') > -1, //是否微信 （2015-01-22新增）
			qq: u.match(/\sQQ/i) == " qq" //是否QQ
		};
	}(),
	language: (navigator.browserLanguage || navigator.language).toLowerCase()
}


/**
 * 数组格式转树状结构
 * @param   {array}     array
 * @param   {String}    id
 * @param   {String}    pid
 * @param   {String}    children
 * @return  {Array}
 */
export const arrayToTree = (array, id = 'id', pid = 'pid', children = 'children') => {
	let data = array.map(item => ({ ...item
	}))
	let result = []
	let hash = {}
	data.forEach((item, index) => {
		hash[data[index][id]] = data[index]
	})

	data.forEach((item) => {
		let hashVP = hash[item[pid]]
		if (hashVP) {
			!hashVP[children] && (hashVP[children] = [])
			hashVP[children].push(item)
		} else {
			result.push(item)
		}
	})
	return result
}
