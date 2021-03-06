<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script type="text/javascript">
	$(function() {
		parent.$.messager.progress('close');
		$('#form').form({
			url : '${pageContext.request.contextPath}/business/controller/AdviceController/addAnalysis.do',
			onSubmit : function() {
				parent.$.messager.progress({
					title : '提示',
					text : '数据处理中，请稍后....'
				});
				var isValid = $(this).form('validate');
				if (!isValid) {
					parent.$.messager.progress('close');
				}
				return isValid;
			},
			success : function(result) {
				parent.$.messager.progress('close');
				result = $.parseJSON(result);
				if (result.success) {
					parent.$.modalDialog.openner_dataGrid.datagrid('reload');//之所以能在这里调用到parent.$.modalDialog.openner_dataGrid这个对象，是因为user.jsp页面预定义好了
					parent.$.modalDialog.handler.dialog('close');
				} else {
					parent.$.messager.alert('错误', result.msg, 'error');
				}
			}
		});
	});
</script>
<div class="easyui-layout" data-options="fit:true,border:false">
	<div data-options="region:'center',border:false" title="" >
		<form id="form" method="post">
			<table class="table table-hover table-condensed">
			<tr>
				<th>农作物</th>
					<td colspan=""><input id="crops_id" name="crops_id" class="easyui-combobox" data-options="required:true,width:120, valueField: 'crops_id',textField: 'crops_name', 
								url: '${pageContext.request.contextPath}/business/controller/CropController/queryCropCombo.do', onSelect: function(rec){ 
							var url ='${pageContext.request.contextPath}/business/controller/PeriodController/queryPeriodComboByCrops.do?crops_id=' + rec.crops_id;
							$('#period_id').combobox('clear');
           				 	$('#period_id').combobox('reload', url);  }" />   </td>
				<th>生育时期</th>
					<td colspan="">
					<input id="period_id" name="period_id" class="easyui-combobox" data-options="required:true,width:320, valueField: 'period_id',textField: 'period_name'"/>   </td>	
								</tr>
								<tr>
					<th>气象要素 </th>
					<td colspan="3"><textarea name="element" rows="" cols="" class="span5"></textarea></td>
					</tr>
					<tr>
					<th>具体因子 </th>
					<td colspan="3"><textarea name="factor" rows="" cols="" class="span5"></textarea></td>
					</tr>
					<tr>
					<th>适宜条件 </th>
					<td colspan="3"><textarea name="conditions" rows="" cols="" class="span5"></textarea></td>
					</tr>
					<tr>
					<th>异常条件及灾害 </th>
					<td colspan="3"><textarea name="content1" rows="10" cols="" class="span5"></textarea></td>
					</tr>
					<tr>
					<th>气象灾害及减灾建议 </th>
					<td colspan="3"><textarea name="content2" rows="10" cols="" class="span5"></textarea></td>
				</tr> 
			</table>
		</form>
	</div>
</div>