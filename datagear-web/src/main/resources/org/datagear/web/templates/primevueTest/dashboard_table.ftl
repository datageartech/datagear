<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_app_name_prefix.ftl">看板</title>
</head>
<body class="p-card no-border">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page-table">
	<div class="page-table-header grid align-items-center">
		<div class="col-12 md:col-3">
			<form action="#">
				<div class="p-inputgroup">
						<p-inputtext type="text" v-model="value1"></p-inputtext>
						<p-button type="submit" icon="pi pi-search" />
				</div>
			</form>
		</div>
		<div class="h-opts col-12 md:col-9 text-right">
			<p-button label="添加" @click="openDialog"></p-button>
			<p-button label="编辑"></p-button>
			<p-button class="p-button-danger">删除</p-button>
		</div>
	</div>
	<div class="page-table-content">
		<p-datatable :value="product.items" :scrollable="true" scroll-height="flex"
			:paginator="true" paginator-template="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
			:rows="10" current-page-report-template="{first}-{last} / {totalRecords}"
			:rows-per-page-options="[10,20,50,100]"
			v-model:selection="product.selecteds" selection-mode="multiple" dataKey="id" striped-rows>
			<p-column selection-mode="multiple" header-style="width:4rem" class="flex-grow-0"></p-column>
			<p-column field="id" header="ID" :sortable="true"></p-column>
			<p-column field="name" header="名称" :sortable="true"></p-column>
			<p-column field="category" header="项目"></p-column>
			<p-column field="quantity" header="创建用户"></p-column>
		</p-datatable>
	</div>
</div>
<script>
(function(po)
{
	po.vueSetup("openDialog", function()
	{
		po.open("/primevue/chartList");
	});
	
	po.vueRef("product",
	{
		items:
		[
			{"id": "1000","code": "f230fh0g3","name": "Bamboo Watch","description": "Product Description","image": "bamboo-watch.jpg","price": 65,"category": "Accessories","quantity": 24,"inventoryStatus": "INSTOCK","rating": 5},
			{"id": "1001","code": "nvklal433","name": "Black Watch","description": "Product Description","image": "black-watch.jpg","price": 72,"category": "Accessories","quantity": 61,"inventoryStatus": "INSTOCK","rating": 4},
			{"id": "1002","code": "zz21cz3c1","name": "Blue Band","description": "Product Description","image": "blue-band.jpg","price": 79,"category": "Fitness","quantity": 2,"inventoryStatus": "LOWSTOCK","rating": 3},
			{"id": "1003","code": "244wgerg2","name": "Blue T-Shirt","description": "Product Description","image": "blue-t-shirt.jpg","price": 29,"category": "Clothing","quantity": 25,"inventoryStatus": "INSTOCK","rating": 5},
			{"id": "1004","code": "h456wer53","name": "Bracelet","description": "Product Description","image": "bracelet.jpg","price": 15,"category": "Accessories","quantity": 73,"inventoryStatus": "INSTOCK","rating": 4},
			{"id": "1005","code": "av2231fwg","name": "Brown Purse","description": "Product Description","image": "brown-purse.jpg","price": 120,"category": "Accessories","quantity": 0,"inventoryStatus": "OUTOFSTOCK","rating": 4},
			{"id": "1006","code": "bib36pfvm","name": "Chakra Bracelet","description": "Product Description","image": "chakra-bracelet.jpg","price": 32,"category": "Accessories","quantity": 5,"inventoryStatus": "LOWSTOCK","rating": 3},
			{"id": "1007","code": "mbvjkgip5","name": "Galaxy Earrings","description": "Product Description","image": "galaxy-earrings.jpg","price": 34,"category": "Accessories","quantity": 23,"inventoryStatus": "INSTOCK","rating": 5},
			{"id": "1008","code": "vbb124btr","name": "Game Controller","description": "Product Description","image": "game-controller.jpg","price": 99,"category": "Electronics","quantity": 2,"inventoryStatus": "LOWSTOCK","rating": 4},
			{"id": "1009","code": "cm230f032","name": "Gaming Set","description": "Product Description","image": "gaming-set.jpg","price": 299,"category": "Electronics","quantity": 63,"inventoryStatus": "INSTOCK","rating": 3},
			{"id": "1010","code": "sbvjkgip5","name": "Galaxy Earrings","description": "Product Description","image": "galaxy-earrings.jpg","price": 34,"category": "Accessories","quantity": 23,"inventoryStatus": "INSTOCK","rating": 5},
			{"id": "1011","code": "gbb124btr","name": "Game Controller","description": "Product Description","image": "game-controller.jpg","price": 99,"category": "Electronics","quantity": 2,"inventoryStatus": "LOWSTOCK","rating": 4},
			{"id": "1012","code": "um230f032","name": "Gaming Set","description": "Product Description","image": "gaming-set.jpg","price": 299,"category": "Electronics","quantity": 63,"inventoryStatus": "INSTOCK","rating": 3}
		],
		selecteds: []
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>