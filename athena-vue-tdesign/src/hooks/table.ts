import type { PageInfo, PrimaryTableCol, TableColumnController, TableProps, TableRowData } from 'tdesign-vue-next';
import type { Ref } from 'vue';
import { computed, reactive, ref, shallowReactive } from 'vue';

interface Table {
  dataList: Array<any>;
  bordered: Ref<boolean>;
  striped: Ref<boolean>;
  tableLoading: Ref<boolean>;
  tableHeight: Ref<number>;
  handleSuccess: (res: any) => Promise<any>;
  useTableColumn: (columns: PrimaryTableCol<TableRowData>[], options: PrimaryTableCol<TableRowData>) => Array<any>;
  selectionColumn: { type: 'selection' };
  indexColumn: {
    title?: string;
    colKey: string;
    width: number;
    type?: 'single' | 'multiple';
  }[];
}

export const useTableHeight = async function (currentIns: any): Promise<number> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const clientHeight = document.querySelector('.main-section')?.getBoundingClientRect().height || 0;
      const tableHeaderHeight = document.querySelector('.ant-table-header')?.getBoundingClientRect().height || 0;
      if (currentIns) {
        let tempHeight = tableHeaderHeight;
        if (currentIns.refs.tableHeaderRef) {
          const header = (currentIns.refs as any).tableHeaderRef.$el;
          tempHeight += header.clientHeight;
        }
        if (currentIns.refs.tableFooterRef) {
          tempHeight += 45;
        }
        resolve(clientHeight - tempHeight);
      }
      resolve(150);
    }, 500);
  });
};

export const useTable = function (): Table {
  const dataList = shallowReactive([]) as Array<any>;
  const tableHeight = ref(200);
  const bordered = ref(false);
  const striped = ref(false);
  const tableLoading = ref(true);
  const handleSuccess = (list: []): Promise<any> => {
    tableLoading.value = false;
    dataList.length = 0;
    dataList.push(...list);
    return Promise.resolve(list);
  };
  return {
    dataList,
    tableHeight,
    bordered,
    striped,
    tableLoading,
    handleSuccess,
    useTableColumn,
    selectionColumn: {
      type: 'selection',
    },
    indexColumn: useTableIndexColumn('multiple'),
  };
};

export const useRowKey = function (propName: string) {
  const rowKey: TableProps['rowKey'] = propName;
  return rowKey;
};
export const useRowSelection = function () {
  const selectedRowKeys = ref<TableProps['selectedRowKeys']>([]);
  const onSelectChange: TableProps['onSelectChange'] = (tempSelectRows, ctx) => {
    selectedRowKeys.value = tempSelectRows;
    console.log(ctx);
  };
  const onClearSelected = () => {
    selectedRowKeys.value = []
  };
  return {
    selectedRowKeys,
    onSelectChange,
    onClearSelected,
  };
};

export const useTableColumn = function (
  columns: PrimaryTableCol<TableRowData>[],
  options: PrimaryTableCol<TableRowData> = {},
) {
  return columns.map((it) => Object.assign(it, options));
};

export const useTableIndexColumn = function (rowSelectType: 'single' | 'multiple') {
  return [
    {
      colKey: 'row-select',
      type: rowSelectType,
      width: 50,
    },
    {
      title: '序号',
      colKey: 'serial-number',
      width: 80,
    },
  ];
};

export const useColumnControllerConfig = function (options: TableColumnController = {}) {
  const {
    placement = 'bottom-left',
    fields = [],
    dialogProps = { preventScrollThrough: true },
    buttonProps = undefined,
    groupColumns = [],
  } = options;

  const columnControllerConfig = computed<TableProps['columnController']>(() => ({
    placement,
    // fields 为空数组时不传（表示全部字段），有值时才传
    ...(fields.length > 0 && { fields }),
    dialogProps,
    ...(buttonProps && { buttonProps }),
    // groupColumns 有值时才传分组配置
    ...(groupColumns.length > 0 && { groupColumns }),
  }));

  return {
    columnControllerConfig,
  };
};

export const usePagination = function (callback: () => void) {
  const paginationInfo = reactive({
    current: 1,
    defaultCurrent: 1,
    pageSize: 10,
    showPageSize: true,
    showPageNumber: true,
    showFirstAndLastPageBtn: true,
    showPreviousAndNextBtn: true,
    showJumper: true,
    pageSizeOptions: [10, 20, 30, 40],
    total: 0,
    onChange(pageInfo: PageInfo) {
      paginationInfo.current = pageInfo.current;
      paginationInfo.pageSize = pageInfo.pageSize;
      callback();
    },
    setTotalSize(totalSize: number) {
      paginationInfo.total = totalSize;
    },
  });
  return paginationInfo;
};
