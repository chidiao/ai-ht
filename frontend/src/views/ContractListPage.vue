<template>
  <AppLayout>
      <PageHeader
        title="合同列表"
        eyebrow="Contracts"
        :breadcrumbs="[{ label: '首页', to: '/dashboard' }, { label: '合同列表' }]"
      >
        <el-button :loading="exporting" :disabled="!canExportContracts(currentRole) || !pagination.total" @click="exportExcel">
          导出筛选结果Excel
        </el-button>
        <el-button type="primary" :icon="Plus" :disabled="!canCreateContract(currentRole)" @click="$router.push('/contracts/new')">
          新建合同
        </el-button>
      </PageHeader>

      <ContractFilters v-model="filters" @search="searchFromFirstPage" @reset="resetFilters" />
      <ContractTable
        :contracts="contracts"
        :loading="loading"
        :can-delete-row="canDeleteRow"
        @detail="(row) => $router.push(`/contracts/${row.id}`)"
        @edit="(row) => $router.push(`/contracts/${row.id}/edit`)"
        @delete="confirmDelete"
      />
      <div class="table-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
  </AppLayout>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import ContractFilters from '../components/ContractFilters.vue'
import ContractTable from '../components/ContractTable.vue'
import PageHeader from '../components/PageHeader.vue'
import { deleteContract, fetchContractPage } from '../api/contracts'
import { useSession } from '../stores/session'
import { canCreateContract, canDeleteContract, canExportContracts } from '../utils/permissions'
import { paymentLabel, statusLabel } from '../utils/contractFormat'

const { currentRole } = useSession()
const route = useRoute()
const contracts = ref([])
const loading = ref(false)
const exporting = ref(false)
const pagination = ref({ page: 1, size: 10, total: 0 })
const filters = ref({
  keyword: '',
  supplierName: '',
  owner: '',
  department: '',
  category: '',
  status: '',
  statuses: [],
  paymentStatus: '',
  archiveStatus: '',
  amountMin: undefined,
  amountMax: undefined,
  signRange: [],
  dueRange: [],
  expiringSoon: false,
  quickFilter: ''
})

async function loadData() {
  loading.value = true
  try {
    const data = await fetchContractPage(buildSearchParams(pagination.value.page - 1, pagination.value.size))
    contracts.value = data.content || []
    pagination.value.total = data.totalElements || 0
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '数据加载失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    keyword: '',
    supplierName: '',
    owner: '',
    department: '',
    category: '',
    status: '',
    statuses: [],
    paymentStatus: '',
    archiveStatus: '',
    amountMin: undefined,
    amountMax: undefined,
    signRange: [],
    dueRange: [],
    expiringSoon: false,
    quickFilter: ''
  }
  searchFromFirstPage()
}

function handleSizeChange() {
  pagination.value.page = 1
  loadData()
}

function searchFromFirstPage() {
  pagination.value.page = 1
  loadData()
}

function canDeleteRow(row) {
  return canDeleteContract(currentRole.value, row)
}

async function confirmDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除合同「${row.name}」吗？删除后该草稿/取消记录及其流程日志将不可恢复。`,
      '删除合同',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    await deleteContract(row.id)
    ElMessage.success('合同已删除')
    if (contracts.value.length === 1 && pagination.value.page > 1) {
      pagination.value.page -= 1
    }
    await loadData()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}

function applyQuickFilter(key, shouldLoad = true) {
  filters.value.quickFilter = ''
  filters.value.status = ''
  filters.value.statuses = []
  filters.value.paymentStatus = ''
  filters.value.archiveStatus = ''
  filters.value.expiringSoon = false

  if (key === 'pendingApproval') {
    filters.value.statuses = ['PENDING_APPROVAL']
  } else if (key === 'pendingPayment') {
    filters.value.quickFilter = 'pendingPayment'
  } else if (key === 'expiringSoon') {
    filters.value.expiringSoon = true
  } else if (key === 'executing') {
    filters.value.statuses = ['EXECUTING']
  }

  if (shouldLoad) {
    loadData()
  }
}

function buildSearchParams(page, size) {
  return {
    keyword: filters.value.keyword || undefined,
    supplierName: filters.value.supplierName || undefined,
    owner: filters.value.owner || undefined,
    department: filters.value.department || undefined,
    category: filters.value.category || undefined,
    status: filters.value.status || undefined,
    statuses: filters.value.statuses?.length ? filters.value.statuses : undefined,
    paymentStatus: filters.value.paymentStatus || undefined,
    archiveStatus: filters.value.archiveStatus || undefined,
    amountMin: filters.value.amountMin ?? undefined,
    amountMax: filters.value.amountMax ?? undefined,
    signStart: filters.value.signRange?.[0],
    signEnd: filters.value.signRange?.[1],
    dueStart: filters.value.dueRange?.[0],
    dueEnd: filters.value.dueRange?.[1],
    expiringSoon: filters.value.expiringSoon || undefined,
    quickFilter: filters.value.quickFilter || undefined,
    page,
    size
  }
}

async function exportExcel() {
  exporting.value = true
  try {
    const exportSize = Math.min(Math.max(pagination.value.total, 1), 100)
    const data = await fetchContractPage(buildSearchParams(0, exportSize))
    const rowsSource = data.content || []
    if (pagination.value.total > 100) {
      ElMessage.warning('当前导出前 100 条筛选结果，可继续缩小筛选条件')
    }
    downloadExcel(rowsSource)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

function downloadExcel(list) {
  const columns = [
    { key: 'contractNo', label: '合同编号', width: 140, type: 'text' },
    { key: 'name', label: '合同名称', width: 260, type: 'text' },
    { key: 'supplierName', label: '供应商', width: 220, type: 'text' },
    { key: 'category', label: '采购品类', width: 120, type: 'text' },
    { key: 'amount', label: '合同金额', width: 130, type: 'money' },
    { key: 'paidAmount', label: '已付金额', width: 130, type: 'money' },
    { key: 'status', label: '合同状态', width: 120, type: 'status' },
    { key: 'paymentStatus', label: '付款状态', width: 120, type: 'payment' },
    { key: 'owner', label: '负责人', width: 100, type: 'text' },
    { key: 'expiryDate', label: '到期日期', width: 120, type: 'date' }
  ]
  const rows = list.map((item) => [
    item.contractNo,
    item.name,
    item.supplierName,
    item.category || '',
    item.amount,
    item.paidAmount,
    statusLabel(item.status),
    paymentLabel(item.paymentStatus),
    item.owner,
    item.expiryDate
  ])
  const worksheet = buildWorksheetXml(columns, rows)
  const blob = createXlsxBlob(worksheet)
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `合同列表-${new Date().toISOString().slice(0, 10)}.xlsx`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${list.length} 条合同`)
}

function buildWorksheetXml(columns, rows) {
  const header = columns.map((column, index) => buildCell(1, index, column.label, 'text', 2)).join('')
  const body = rows.map((row, rowIndex) => {
    const rowNumber = rowIndex + 2
    return `<row r="${rowNumber}">${row.map((value, columnIndex) => buildCell(rowNumber, columnIndex, value, columns[columnIndex].type)).join('')}</row>`
  }).join('')
  return `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <cols>${columns.map((column, index) => `<col min="${index + 1}" max="${index + 1}" width="${Math.round(column.width / 7)}" customWidth="1"/>`).join('')}</cols>
  <sheetData><row r="1">${header}</row>${body}</sheetData>
</worksheet>`
}

function buildCell(rowNumber, columnIndex, value, type, style = 0) {
  const ref = `${columnName(columnIndex + 1)}${rowNumber}`
  if (type === 'money') {
    return `<c r="${ref}" s="1"><v>${Number(value || 0).toFixed(2)}</v></c>`
  }
  return `<c r="${ref}" s="${style}" t="inlineStr"><is><t>${escapeXml(value || '')}</t></is></c>`
}

function columnName(index) {
  let name = ''
  let current = index
  while (current > 0) {
    const remainder = (current - 1) % 26
    name = String.fromCharCode(65 + remainder) + name
    current = Math.floor((current - 1) / 26)
  }
  return name
}

function createXlsxBlob(worksheetXml) {
  const files = [
    ['[Content_Types].xml', `<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/></Types>`],
    ['_rels/.rels', `<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>`],
    ['xl/workbook.xml', `<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="合同列表" sheetId="1" r:id="rId1"/></sheets></workbook>`],
    ['xl/_rels/workbook.xml.rels', `<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>`],
    ['xl/styles.xml', `<?xml version="1.0" encoding="UTF-8" standalone="yes"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><numFmts count="1"><numFmt numFmtId="164" formatCode="¥#,##0.00"/></numFmts><fonts count="2"><font><sz val="11"/><name val="Microsoft YaHei"/></font><font><b/><sz val="11"/><name val="Microsoft YaHei"/></font></fonts><fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FFF1F5F9"/><bgColor indexed="64"/></patternFill></fill></fills><borders count="2"><border><left/><right/><top/><bottom/><diagonal/></border><border><left style="thin"><color rgb="FFCBD5E1"/></left><right style="thin"><color rgb="FFCBD5E1"/></right><top style="thin"><color rgb="FFCBD5E1"/></top><bottom style="thin"><color rgb="FFCBD5E1"/></bottom><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="3"><xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0"/><xf numFmtId="164" fontId="0" fillId="0" borderId="1" xfId="0" applyNumberFormat="1"/><xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1"/></cellXfs><cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles></styleSheet>`],
    ['xl/worksheets/sheet1.xml', worksheetXml]
  ]
  return new Blob([createZip(files)], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
}

function createZip(files) {
  const encoder = new TextEncoder()
  const chunks = []
  const central = []
  let offset = 0
  files.forEach(([name, content]) => {
    const nameBytes = encoder.encode(name)
    const data = encoder.encode(content)
    const crc = crc32(data)
    chunks.push(zipLocalHeader(nameBytes, data, crc), data)
    central.push(zipCentralHeader(nameBytes, data, crc, offset))
    offset += 30 + nameBytes.length + data.length
  })
  const centralSize = central.reduce((sum, item) => sum + item.length, 0)
  const centralOffset = offset
  chunks.push(...central, zipEndRecord(files.length, centralSize, centralOffset))
  return new Blob(chunks)
}

function zipLocalHeader(nameBytes, data, crc) {
  const view = new DataView(new ArrayBuffer(30 + nameBytes.length))
  writeHeader(view, 0x04034b50, 20, crc, data.length, nameBytes.length)
  new Uint8Array(view.buffer).set(nameBytes, 30)
  return view.buffer
}

function zipCentralHeader(nameBytes, data, crc, offset) {
  const view = new DataView(new ArrayBuffer(46 + nameBytes.length))
  writeHeader(view, 0x02014b50, 20, crc, data.length, nameBytes.length)
  view.setUint16(4, 20, true)
  view.setUint32(42, offset, true)
  new Uint8Array(view.buffer).set(nameBytes, 46)
  return view.buffer
}

function writeHeader(view, signature, version, crc, size, nameLength) {
  view.setUint32(0, signature, true)
  view.setUint16(signature === 0x02014b50 ? 6 : 4, version, true)
  view.setUint32(signature === 0x02014b50 ? 16 : 14, crc, true)
  view.setUint32(signature === 0x02014b50 ? 20 : 18, size, true)
  view.setUint32(signature === 0x02014b50 ? 24 : 22, size, true)
  view.setUint16(signature === 0x02014b50 ? 28 : 26, nameLength, true)
}

function zipEndRecord(fileCount, centralSize, centralOffset) {
  const view = new DataView(new ArrayBuffer(22))
  view.setUint32(0, 0x06054b50, true)
  view.setUint16(8, fileCount, true)
  view.setUint16(10, fileCount, true)
  view.setUint32(12, centralSize, true)
  view.setUint32(16, centralOffset, true)
  return view.buffer
}

function crc32(data) {
  let crc = -1
  for (let index = 0; index < data.length; index += 1) {
    crc = (crc >>> 8) ^ crcTable[(crc ^ data[index]) & 0xff]
  }
  return (crc ^ -1) >>> 0
}

const crcTable = Array.from({ length: 256 }, (_, index) => {
  let value = index
  for (let bit = 0; bit < 8; bit += 1) {
    value = value & 1 ? 0xedb88320 ^ (value >>> 1) : value >>> 1
  }
  return value >>> 0
})

function escapeXml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

function applyRouteQuery() {
  filters.value.statuses = []
  filters.value.paymentStatus = ''
  filters.value.quickFilter = ''
  filters.value.expiringSoon = false

  if (route.query.status) {
    filters.value.statuses = [String(route.query.status)]
  }
  if (route.query.quick) {
    applyQuickFilter(String(route.query.quick), false)
  }
}

watch(
  () => route.query,
  () => {
    applyRouteQuery()
    searchFromFirstPage()
  },
  { immediate: true }
)
</script>
