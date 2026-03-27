$(function() {

	// init date tables
	var userListTable = $("#user_list").dataTable({
		"deferRender": true,
		"processing" : true, 
	    "serverSide": true,
		"ajax": {
			url: base_url + "/user/pageList",
			type:"post",
	        data : function ( d ) {
	        	var obj = {};
                obj.username = $('#username').val();
                obj.role = $('#role').val();
	        	obj.start = d.start;
	        	obj.length = d.length;
                return obj;
            }
	    },
	    "searching": false,
	    "ordering": false,
	    //"scrollX": true,	// scroll x，close self-adaption
	    "columns": [
	                {
	                	"data": 'id',
						"visible" : false,
						"width":'10%'
					},
	                {
	                	"data": 'username',
						"visible" : true,
						"width":'20%'
					},
	                {
	                	"data": 'password',
						"visible" : false,
                        "width":'20%',
                        "render": function ( data, type, row ) {
                            return '*********';
                        }
					},
					{
						"data": 'role',
						"visible" : true,
						"width":'10%',
                        "render": function ( data, type, row ) {
                            if (data == 1) {
                                return I18n.user_role_admin
                            } else {
                                return I18n.user_role_normal
                            }
                        }
					},
	                {
	                	"data": 'permission',
						"width":'10%',
	                	"visible" : false
	                },
	                {
						"data": I18n.system_opt ,
						"width":'15%',
	                	"render": function ( data, type, row ) {
	                		return function(){
								// html
                                tableData['key'+row.id] = row;
								var html = '<p id="'+ row.id +'" >'+
									'<button class="btn btn-warning btn-xs update" type="button">'+ I18n.system_opt_edit +'</button>  '+
									'<button class="btn btn-danger btn-xs delete" type="button">'+ I18n.system_opt_del +'</button>  '+
									'<button class="btn btn-info btn-xs bind2fa" type="button">绑定2FA</button>  '+
									'<button class="btn btn-default btn-xs unbind2fa" type="button">解绑2FA</button>  '+
									'</p>';

	                			return html;
							};
	                	}
	                }
	            ],
		"language" : {
			"sProcessing" : I18n.dataTable_sProcessing ,
			"sLengthMenu" : I18n.dataTable_sLengthMenu ,
			"sZeroRecords" : I18n.dataTable_sZeroRecords ,
			"sInfo" : I18n.dataTable_sInfo ,
			"sInfoEmpty" : I18n.dataTable_sInfoEmpty ,
			"sInfoFiltered" : I18n.dataTable_sInfoFiltered ,
			"sInfoPostFix" : "",
			"sSearch" : I18n.dataTable_sSearch ,
			"sUrl" : "",
			"sEmptyTable" : I18n.dataTable_sEmptyTable ,
			"sLoadingRecords" : I18n.dataTable_sLoadingRecords ,
			"sInfoThousands" : ",",
			"oPaginate" : {
				"sFirst" : I18n.dataTable_sFirst ,
				"sPrevious" : I18n.dataTable_sPrevious ,
				"sNext" : I18n.dataTable_sNext ,
				"sLast" : I18n.dataTable_sLast
			},
			"oAria" : {
				"sSortAscending" : I18n.dataTable_sSortAscending ,
				"sSortDescending" : I18n.dataTable_sSortDescending
			}
		}
	});

    // table data
    var tableData = {};

	// search btn
	$('#searchBtn').on('click', function(){
        userListTable.fnDraw();
	});
	
	// job operate
	$("#user_list").on('click', '.delete',function() {
		var id = $(this).parent('p').attr("id");

		layer.confirm( I18n.system_ok + I18n.system_opt_del + '?', {
			icon: 3,
			title: I18n.system_tips ,
            btn: [ I18n.system_ok, I18n.system_cancel ]
		}, function(index){
			layer.close(index);

			$.ajax({
				type : 'POST',
				url : base_url + "/user/remove",
				data : {
					"id" : id
				},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
                        layer.msg( I18n.system_success );
						userListTable.fnDraw(false);
					} else {
                        layer.msg( data.msg || I18n.system_opt_del + I18n.system_fail );
					}
				}
			});
		});
	});

	// add role
    $("#addModal .form input[name=role]").change(function () {
		var role = $(this).val();
		if (role == 1) {
            $("#addModal .form input[name=permission]").parents('.form-group').hide();
		} else {
            $("#addModal .form input[name=permission]").parents('.form-group').show();
		}
        $("#addModal .form input[name='permission']").prop("checked",false);
    });

    jQuery.validator.addMethod("myValid01", function(value, element) {
        var length = value.length;
        var valid = /^[a-z][a-z0-9]*$/;
        return this.optional(element) || valid.test(value);
    }, I18n.user_username_valid );

	// add
	$(".add").click(function(){
		$('#addModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var addModalValidate = $("#addModal .form").validate({
		errorElement : 'span',  
        errorClass : 'help-block',
        focusInvalid : true,  
        rules : {
            username : {
				required : true,
                rangelength:[4, 20],
                myValid01: true
			},
            password : {
                required : true,
                rangelength:[4, 20]
            }
        }, 
        messages : {
            username : {
            	required : I18n.system_please_input + I18n.user_username,
                rangelength: I18n.system_lengh_limit + "[4-20]"
            },
            password : {
                required : I18n.system_please_input + I18n.user_password,
                rangelength: I18n.system_lengh_limit + "[4-20]"
            }
        },
		highlight : function(element) {  
            $(element).closest('.form-group').addClass('has-error');  
        },
        success : function(label) {  
            label.closest('.form-group').removeClass('has-error');  
            label.remove();  
        },
        errorPlacement : function(error, element) {  
            element.parent('div').append(error);  
        },
        submitHandler : function(form) {

            var permissionArr = [];
            $("#addModal .form input[name=permission]:checked").each(function(){
                permissionArr.push($(this).val());
            });

			var paramData = {
				"username": $("#addModal .form input[name=username]").val(),
                "password": $("#addModal .form input[name=password]").val(),
                "role": $("#addModal .form input[name=role]:checked").val(),
                "permission": permissionArr.join(',')
			};

        	$.post(base_url + "/user/add", paramData, function(data, status) {
    			if (data.code == "200") {
					$('#addModal').modal('hide');

                    layer.msg( I18n.system_add_suc );
                    userListTable.fnDraw();
    			} else {
					layer.open({
						title: I18n.system_tips ,
                        btn: [ I18n.system_ok ],
						content: (data.msg || I18n.system_add_fail),
						icon: '2'
					});
    			}
    		});
		}
	});
	$("#addModal").on('hide.bs.modal', function () {
		$("#addModal .form")[0].reset();
		addModalValidate.resetForm();
		$("#addModal .form .form-group").removeClass("has-error");
		$(".remote_panel").show();	// remote

        $("#addModal .form input[name=permission]").parents('.form-group').show();
	});

    // update role
    $("#updateModal .form input[name=role]").change(function () {
        var role = $(this).val();
        if (role == 1) {
            $("#updateModal .form input[name=permission]").parents('.form-group').hide();
        } else {
            $("#updateModal .form input[name=permission]").parents('.form-group').show();
        }
        $("#updateModal .form input[name='permission']").prop("checked",false);
    });

	// update
	$("#user_list").on('click', '.update',function() {

        var id = $(this).parent('p').attr("id");
        var row = tableData['key'+id];

		// base data
		$("#updateModal .form input[name='id']").val( row.id );
		$("#updateModal .form input[name='username']").val( row.username );
		$("#updateModal .form input[name='password']").val( '' );
		$("#updateModal .form input[name='role'][value='"+ row.role +"']").click();
        var permissionArr = [];
        if (row.permission) {
            permissionArr = row.permission.split(",");
		}
        $("#updateModal .form input[name='permission']").each(function () {
            if($.inArray($(this).val(), permissionArr) > -1) {
                $(this).prop("checked",true);
            } else {
                $(this).prop("checked",false);
            }
        });

		// show
		$('#updateModal').modal({backdrop: false, keyboard: false}).modal('show');
	});

// ========== 新增：2FA绑定按钮点击事件 ==========
	$("#user_list").on('click', '.bind2fa', function() {
		var id = $(this).parent('p').attr("id");
		var row = tableData['key'+id];
		bindTwoFactor(row.id, row.username);
	});

	// ========== 新增：2FA解绑按钮点击事件 ==========
	$("#user_list").on('click', '.unbind2fa', function() {
		var id = $(this).parent('p').attr("id");
		unbindTwoFactor(id);
	});

	var updateModalValidate = $("#updateModal .form").validate({
		errorElement : 'span',  
        errorClass : 'help-block',
        focusInvalid : true,
		highlight : function(element) {
            $(element).closest('.form-group').addClass('has-error');  
        },
        success : function(label) {  
            label.closest('.form-group').removeClass('has-error');  
            label.remove();  
        },
        errorPlacement : function(error, element) {  
            element.parent('div').append(error);  
        },
        submitHandler : function(form) {

            var permissionArr =[];
            $("#updateModal .form input[name=permission]:checked").each(function(){
                permissionArr.push($(this).val());
            });

            var paramData = {
                "id": $("#updateModal .form input[name=id]").val(),
                "username": $("#updateModal .form input[name=username]").val(),
                "password": $("#updateModal .form input[name=password]").val(),
                "role": $("#updateModal .form input[name=role]:checked").val(),
                "permission": permissionArr.join(',')
            };

            $.post(base_url + "/user/update", paramData, function(data, status) {
                if (data.code == "200") {
                    $('#updateModal').modal('hide');

                    layer.msg( I18n.system_update_suc );
                    userListTable.fnDraw();
                } else {
                    layer.open({
                        title: I18n.system_tips ,
                        btn: [ I18n.system_ok ],
                        content: (data.msg || I18n.system_update_fail),
                        icon: '2'
                    });
                }
            });
		}
	});
	$("#updateModal").on('hide.bs.modal', function () {
        $("#updateModal .form")[0].reset();
        updateModalValidate.resetForm();
        $("#updateModal .form .form-group").removeClass("has-error");
        $(".remote_panel").show();	// remote

        $("#updateModal .form input[name=permission]").parents('.form-group').show();
	});

// ========== 新增：2FA相关函数 ==========
// 当前操作的用户ID和密钥
	var currentUserId = null;
	var currentSecretKey = null;
	var currentUserName = null;

	/**
	 * 绑定2FA
	 */
	function bindTwoFactor(userId, userName) {
		currentUserId = userId;
		currentUserName = userName;

		$('#bindTwoFactorDialog').modal('show');
		$('#verifyCodeInput').val('');
		$('#secretKeyText').val('');
		$('#qrCodeImg').attr('src', '');

		$.ajax({
			url: base_url + '/twoFactor/generate',
			type: 'POST',
			data: { userId: userId },
			dataType: 'json',
			success: function(res) {
				if (res.code === 200) {
					currentSecretKey = res.content.secretKey;
					$('#secretKeyText').val(currentSecretKey);

					var qrCodeUrl = res.content.qrCodeUrl;
					var qrImgUrl = 'https://quickchart.io/qr?size=200&text=' + encodeURIComponent(qrCodeUrl);
					$('#qrCodeImg').attr('src', qrImgUrl);
				} else {
					layer.msg(res.msg);
					$('#bindTwoFactorDialog').modal('hide');
				}
			},
			error: function() {
				layer.msg('生成二维码失败');
				$('#bindTwoFactorDialog').modal('hide');
			}
		});
	}

	/**
	 * 确认绑定2FA
	 */
	$('#confirmBindBtn').click(function() {
		var verifyCode = $('#verifyCodeInput').val();

		if (!verifyCode || verifyCode.length !== 6) {
			layer.msg('请输入6位验证码');
			return;
		}

		$.ajax({
			url: base_url + '/twoFactor/enable',
			type: 'POST',
			data: {
				userId: currentUserId,
				secretKey: currentSecretKey,
				verifyCode: verifyCode
			},
			dataType: 'json',
			success: function(res) {
				if (res.code === 200) {
					layer.msg('绑定成功');
					$('#bindTwoFactorDialog').modal('hide');
					window.location.reload();
				} else {
					layer.msg(res.msg);
				}
			},
			error: function() {
				layer.msg('绑定失败');
			}
		});
	});

	/**
	 * 解绑2FA
	 */
	function unbindTwoFactor(userId) {
		layer.prompt({
			title: '请输入Google Authenticator验证码确认解绑',
			formType: 0,
			inputType: 'number',
			inputPlaceholder: '请输入6位验证码',
			area: ['400px', '150px']
		}, function(verifyCode, index) {
			layer.close(index);

			if (!verifyCode || verifyCode.length !== 6) {
				layer.msg('请输入6位验证码');
				return;
			}

			$.ajax({
				url: base_url + '/twoFactor/disable',
				type: 'POST',
				data: {
					userId: userId,
					verifyCode: verifyCode
				},
				dataType: 'json',
				success: function(res) {
					if (res.code === 200) {
						layer.msg('解绑成功');
						window.location.reload();
					} else {
						layer.msg(res.msg);
					}
				},
				error: function() {
					layer.msg('解绑失败');
				}
			});
		});
	}
});
