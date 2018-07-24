/**
 * Created by WXP22 on 2018/3/20.
 */
console.log("Vue 脚本初始化..............");
var appListProduction = new Vue(
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
            page: 1,  //显示的是哪一页
            pageSize: 10, //每一页显示的数据条数
            total: 0, //记录总数
            maxPage:1,  //最大页数
            msgText:"请重新登录",
            msgWindow:false,
            loginBtnUl : true,
            loginIdUl: false,
            list: [],
            pageInfo:{
            	currentIndex:1,
            	totalPage:5,
            	pageSize:10
            }
        },
        watch:{
        	loginId:function(newVal,oldVal){
        		var vm = this;
    			if( oldVal != newVal){
    				vm.getOrderListByLoginId(1);
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
            changeAutoLogin: function () {
                var vm = this;
                vm.autoLogin = !vm.autoLogin;
            },
           
          //pagehandler方法 跳转到page页
            pageHandler: function (page) {
            	//here you can do custom state update
            	var vm=this;
            	vm.page = page;
            	vm.getOrderListByLoginId(1);
            },
            closeMsgWindow: function(){
            	var vm = this;
                vm.msgWindow = false;
                vm.msgText = '';
            },
            getOrderListByLoginId:function(pageIndex){
            	var vm = this;
            	var param = {};
            	var url = "../../orders/getOrderListByLoginId";
            	param.pageIndex=pageIndex-1;
            	param.loginId=vm.loginId;
            	axios.post(url,param).then(
            			function(response){
            				console.log(response.data.content);
            				var list = response.data;
            				if( list != ''){
            					vm.list=list.content;
            					vm.total=list.pageInfo.total;
            				}
            				
            	})
            }
        },
        created:function(){
        	var vm = this;
        	if(vm.loginId != ''){
        		vm.pageHandler(1);
            	vm.getOrderListByLoginId(1);
        	}else{
        		vm.msgWindow=true;
        	}
        }
    }
);
function showLoginId(loginId, opt){
	var vm = appListProduction;
	vm.mask = false;
	vm.loginId = loginId;
	vm.loginIdUl = true;
	vm.loginBtnUl = false;
	
	if(opt == 'login'){
//		initData();
	}
}
function clearLoginId() {
    var vm = appListProduction;
    vm.loginId = '';
    vm.loginIdUl = false;
    vm.loginBtnUl = true;
}
console.log("Vue 脚本绑定渲染完成..............");