/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
angular.module('flowableModeler').controller('FlowableZcformReferenceDisplayCtrl',
    [ '$scope', '$modal', '$http', function($scope, $modal, $http) {
    
    	//TODO 获取中车单个表单接口
    	var url = "http://192.168.0.142:8299/vue/umppDesignProcMeta/";
    if ($scope.property && $scope.property.value && $scope.property.value.id) {
   		$http.get(url + $scope.property.value.id)
            .success(
                function(response) {
                    $scope.zcform = {
                    	id: response.data.id,
                    	name: response.data.name
                    };
                });
    }
	
}]);

angular.module('flowableModeler').controller('FlowableZcformReferenceCtrl',
    [ '$scope', '$modal', '$http', function($scope, $modal, $http) {
	
    
     // Config for the modal window
     var opts = {
        template:  'editor-app/configuration/properties/zcform-reference-popup.html?version=' + Date.now(),
         scope: $scope
     };

     // Open the dialog
     _internalCreateModal(opts, $modal, $scope);
}]);

angular.module('flowableModeler').controller('FlowableZcformReferencePopupCtrl',
    [ '$rootScope', '$scope', '$http', '$location', 'editorManager', function($rootScope, $scope, $http, $location, editorManager) {
	 
    $scope.selectedDataItems = undefined;
    
    $scope.gridOptions = {
    	selectedItems: $scope.selectedDataItems,
        headerRowHeight: 28,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        modifierKeysToMultiSelect: false,
        enableHorizontalScrollbar: 0,
		enableColumnMenus: false,
		enableSorting: false,
        columnDefs: [{ field: 'id', displayName: 'id', width: 50, cellTemplate: '<div class="ui-grid-cell-contents">{{"" + row.entity[col.field] | translate}}</div>' },
            { field: 'name', displayName: '名称', cellTemplate: '<div class="ui-grid-cell-contents">{{"" + row.entity[col.field] | translate}}</div>'},
            { field: 'code', displayName: '编码', cellTemplate: '<div class="ui-grid-cell-contents">{{"" + row.entity[col.field] | translate}}</div>'},
            { field: 'url', displayName: 'url', cellTemplate: '<div class="ui-grid-cell-contents">{{"" + row.entity[col.field] | translate}}</div>'}
        ]
    };
    
    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
        $scope.$hide();
    };
    
    // Selecting/deselecting a subprocess
    $scope.selectForm = function(form, $event) {
   	 	$event.stopPropagation();
   	 	if ($scope.selectedForm && $scope.selectedForm.id && form.id == $scope.selectedForm.id) {
   	 		// un-select the current selection
   	 		$scope.selectedForm = null;
   	 	} else {
   	 		$scope.selectedForm = form;
   	 	}
    };
    
    $scope.gridOptions.onRegisterApi = function(gridApi) {
        //set gridApi on scope
        $scope.gridApi = gridApi;
        gridApi.selection.on.rowSelectionChanged($scope, function(row) {
            if (row.isSelected) {
                $scope.selectedDataItems = row.entity;
            } else {
                $scope.selectedDataItems = undefined;
            }
        });
    };
    
    // Saving the selected value
    $scope.save = function() {
   	 	if ($scope.selectedDataItems) {
   	 		$scope.property.value = {
   	 			'id' : $scope.selectedDataItems.id, 
   	 			'name' : $scope.selectedDataItems.name,
   	 			'code' : $scope.selectedDataItems.code
   	 		};
   	 		
   	 	} else {
   	 		$scope.property.value = null; 
   	 	}
   	 	$scope.updatePropertyInModel($scope.property);
   	 	
   	 	// TODO 保存表单信息 发布事件
		$scope.$emit('zcformSubmit', {
   	 			'id' : $scope.selectedDataItems.id, 
   	 			'name' : $scope.selectedDataItems.name,
   	 			'url' : $scope.selectedDataItems.url,
   	 			'code' : $scope.selectedDataItems.code
   	 		});
   	 	
   	 	$scope.close();
    };
    
   
    $scope.cancel = function() {
        $scope.close();
    };

    $scope.loadData = function() {
        var modelMetaData = editorManager.getBaseModelData();
        // TODO 获取中车表单的接口
        //var url = "http://localhost:8081/api/testgrid";
        var url = "http://192.168.0.142:8299/vue/umppDesignProcMeta/listAll";
        $http.get(url)
            .success(
                function(response) {
                	$scope.gridOptions = {
                	        data: response.data,
                	    };
                })
            .error(
                function(data, status, headers, config) {
                	alert("接口不通");
                });
    };

    $scope.loadData();
}]);
