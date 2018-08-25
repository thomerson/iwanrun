/**
 * Created by WXP22 on 2018/3/20.
 */
console.log("Vue 脚本初始化..............");
var appMyAccount = new Vue(
    {
        el: "#container",
        data: {
            mask: false,
            loginWindow: false,
            autoLogin: false,
            loginTitle: '用户登录',
            loginId:'',
            loginToken:'uuixooppasyytvdbftrraskm',
            loginRole:{id:1,role:'采购方'},
            msgText:"请重新登录",
            msgWindow:false,
            nickname:'',
            headimg:'',
            loginBtnUl : true,
            loginIdUl: false,
            adviserAccount:{},
            caseDraft:{},
            appointment:{},
            projectConclude:{},
            resultBtn:true,
            order: {
                orderid: '40020171014',
                createdate: '2017年10月12日',
                status: '已完成',
                orderType:'户外拓展',
                location:'浙西大峡谷',
                duration:'2天',
                simulatedPrice:'800元/人',
                other:'其他需求',
                counselor: '咨询师001',
                orderAttachments:[
                	{
                		text:'合作意向书.pdf',
                		url:'./合作意向书.pdf'
                	},
                	{
                		text:'项目汇总(案例).pdf',
                		url:'./项目汇总(案例).pdf'
                	}
                ]
            }
        },
        created:function(){
        	var vm = this;
        	vm.getOrderInfo();
        	
        	
        },
        watch:{
        	loginId:function(newVal,oldVal){
        		var vm = this;
    			if( oldVal != newVal){
    				vm.getUserInfo();
    				vm.msgWindow=false;
    			}
        	}
        },
        methods: {
        	showLogin: function (message) {
                console.log("v-on  click method :showLogin");
                lrApp.showLogin(message);
            },
            closeLogin: function () {
                console.log("v-on  click method :closeLogin");
                var vm = this;
                vm.mask = false;
                vm.loginWindow = false;
            },
            closeMsgWindow: function(){
            	var vm = this;
                vm.msgWindow = false;
                vm.msgText = '';
            },
            changeAutoLogin: function () {
                var vm = this;
                vm.autoLogin = !vm.autoLogin;
            },
            getOrderInfo:function(){
            	var vm = this;
            	var id = getQueryString("id");
            	var url = "../../orders/get";
            	axios.post(url,{id:id}).then(
            			function(response){
            				var data = response.data;
            				if(data!=''){
            					vm.order=data.orders;
            					vm.order.activityStart=parseDateStr(vm.order.activityStart);
            					vm.order.createTime=parseDateStr(vm.order.createTime);
            					vm.adviserAccount=data.adviserAccount;
            					if(data.orders.orderStatusCode==3){
            						vm.resultBtn=false;
            					}
            					if( data.caseDraft != ''){
            						vm.caseDraft = data.caseDraft;
            					}
            					if( data.appointment != ''){
            						vm.appointment = data.appointment;
            					}
            					if( data.projectConclude != ''){
            						vm.projectConclude = data.projectConclude;
            					}
            					
            				}
            				
            				
            				
            	})
            	
            },
            goBackOrder:function(){
            	window.location.href="./orderlist.html";
            },
            getUserInfo:function(){
            	var vm = this;
            	var url = "../../purchaserAccount/findMixedByLoginId";
            	axios.post(url).then(
            			function(response){
            				console.log(response.data.content);
            				var data = response.data;
            				if( data != ''){
            					var headImgs = data.headImgs;
                				var info = data.userInfo;
                				if(info != '' && info != undefined){
                					vm.nickname = info.name+'，您好';
                				}
                				if (headImgs != '' && headImgs != undefined && headImgs.length > 0) {
                					vm.headimg = headImgs[0].filePath;
                				}
            				}
            	})
            },
            schemeFileUpload:function($event){
            	var vm = this;
            	var url = "/iwantrun/remote/fileupload";
            	var file = $event.target.files[0];
            	var call = function(param){
            		vm.saveFileOrderAttach(param,'case_draft');
            	}
            	fileUpload('uploadedSchemeFile',url,call,file);
            	
            },
            appointFileUpload:function($event){
            	var vm = this;
            	var url = "/iwantrun/remote/fileupload";
            	var file = $event.target.files[0];
            	var call = function(param){
            		vm.saveFileOrderAttach(param,'appointment');
            	}
            	fileUpload('uploadedAppointFile',url,call,file);
            	
            },
            projectFileUpload:function($event){
            	var vm = this;
            	var url = "/iwantrun/remote/fileupload";
            	var file = $event.target.files[0];
            	var call = function(param){
            		vm.saveFileOrderAttach(param,'project_conclude');
            	}
            	fileUpload('uploadedProjectFile',url,call,file);
            	
            },
//            appointmentFileUpload:function($event){
//            	var vm = this;
//            	var url = "/iwantrun/remote/fileupload";
//            	var file = $event.target.files[0];
//            	var call = function(param){
//            		vm.saveFileOrderAttach(param,'appointment');
//            	}
//            	fileUpload('uploadedSchemeFile',url,call,file);
//            	
//            },
//            projectFileUpload:function($event){
//            	var vm = this;
//            	var url = "/iwantrun/remote/fileupload";
//            	var file = $event.target.files[0];
//            	var call = function(param){
//            		vm.saveFileOrderAttach(param,'project_conclude');
//            	}
//            	fileUpload('uploadedSchemeFile',url,call,file);
//            	
//            },
            saveFileOrderAttach:function(filePath,pagePath){
            	var vm = this;
            	if(filePath != ''){
            		var url = "../../orders/saveFileOrderAttach";
            		var orderId = vm.order.id;
                	axios.post(url,{orderId:orderId,loginId:vm.loginId,filePath:filePath,pagePath:pagePath}).then(
                			function(response){
                				var data = response.data;
                				vm.msgWindow=true;
                				if( data == 'success'){
                					vm.msgText="上传成功";
                				}else{
                					vm.msgText="上传失败";
                				}
                	})
            	}
            },
            orderResultClick:function(resultCode){
            	var vm = this;
        		var url = "../../orders/orderResultClick";
        		var orderId = vm.order.id;
            	axios.post(url,{orderId:orderId,resultCode:resultCode}).then(
            			function(response){
            				var data = response.data;
            				if( data == 'success'){
            					vm.msgWindow=true;
            					vm.msgText="操作成功";
            					vm.resultBtn=false;
            				}else{
            					vm.msgWindow=true;
            					vm.msgText="操作失败";
            				}
            				
            	})
            },
        }
    }
);
function showLoginId(loginId, opt){
	var vm = appMyAccount;
	vm.mask = false;
	vm.loginId = loginId;
	vm.loginIdUl = true;
	vm.loginBtnUl = false;
	
	if(opt == 'login'){
//		initData();
	}
}
function clearLoginId() {
    var vm = appMyAccount;
    vm.loginId = '';
    vm.loginIdUl = false;
    vm.loginBtnUl = true;
}
//获取url地址的参数值
function getQueryString(name) { 
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"); 
	var r = window.location.search.substr(1).match(reg); 
	if (r != null) 
		return unescape(r[2]); 
	return null; 
} 

console.log("Vue 脚本绑定渲染完成..............");