function isUnauthorizedError(error) {
    if (!error) {
        return false;
    }
    const code = Number(error.code || error.status || error.statusCode);
    if (code === 401 || code === 403) {
        return true;
    }
    const message = `${error.message || error.msg || ''}`.toLowerCase();
    return message.indexOf('unauthorized') >= 0 || message.indexOf('refresh token') >= 0;
}

function formatSenderLabel(sender) {
    if (!sender || typeof sender !== 'object') {
        return '';
    }
    return `${sender.name || ''} ${sender.phone || ''}`.trim();
}

function buildSenderOptions(senderList) {
    return ['新增'].concat((senderList || []).map(formatSenderLabel));
}

function buildSenderDisplay(senderList, selectedSenderIndex, defaultText) {
    if (!selectedSenderIndex || selectedSenderIndex <= 0) {
        return defaultText;
    }
    const row = (senderList || [])[selectedSenderIndex - 1];
    return row ? formatSenderLabel(row) : defaultText;
}

function buildSenderViewState(senderList, selectedSenderIndex, defaultText) {
    const list = Array.isArray(senderList) ? senderList : [];
    return {
        senderList: list,
        senderOptions: buildSenderOptions(list),
        selectedSenderIndex: selectedSenderIndex > 0 ? selectedSenderIndex : 0,
        selectedSenderDisplay: buildSenderDisplay(list, selectedSenderIndex, defaultText),
    };
}

function getSenderSelectionAfterDelete(params) {
    const selectedSenderIndex = Number(params.selectedSenderIndex) || 0;
    const deleteIndex = Number(params.deleteIndex);
    const newList = params.newList || [];
    if (newList.length === 0) {
        return 0;
    }
    if (deleteIndex + 1 === selectedSenderIndex) {
        return deleteIndex > 0 ? deleteIndex : 1;
    }
    if (deleteIndex + 1 < selectedSenderIndex) {
        return selectedSenderIndex - 1;
    }
    return selectedSenderIndex;
}

function buildSenderStateAfterDelete(params) {
    const newList = Array.isArray(params.newList) ? params.newList : [];
    const newIndex = getSenderSelectionAfterDelete({
        selectedSenderIndex: params.selectedSenderIndex,
        deleteIndex: params.deleteIndex,
        newList,
    });
    return buildSenderViewState(newList, newIndex, params.defaultText);
}

function buildOpenSenderListPopupState() {
    return { showSenderListPopup: true };
}

function buildCloseSenderListPopupState() {
    return { showSenderListPopup: false };
}

function buildOpenSenderModalState() {
    return {
        showSenderModal: true,
        modalSenderName: '',
        modalSenderPhone: '',
        showSenderListPopup: false,
    };
}

function buildCloseSenderModalState() {
    return {
        showSenderModal: false,
        modalSenderName: '',
        modalSenderPhone: '',
    };
}

function buildSenderSelectState(senderList, selectedIndex, defaultText) {
    const selectedSenderIndex = selectedIndex + 1;
    return {
        selectedSenderIndex,
        selectedSenderDisplay: buildSenderDisplay(senderList, selectedSenderIndex, defaultText),
        showSenderListPopup: false,
    };
}

function validateSenderInput(name, phone, nameLabel = '姓名') {
    if (!name) {
        return `请输入${nameLabel}`;
    }
    if (!phone) {
        return '请输入手机号';
    }
    return '';
}

function normalizeLogisticsSuggestList(list) {
    return (list || [])
        .map((item) => (item && item.name ? item.name : ''))
        .filter((name) => !!name);
}

function validateLogisticsSelected(logisticsFilter, logisticsSelected, required) {
    const filterValue = (logisticsFilter || '').trim();
    const selectedValue = (logisticsSelected || '').trim();
    if (filterValue && !selectedValue) {
        return '请从列表中选择物流公司';
    }
    if (selectedValue && filterValue !== selectedValue) {
        return '请从列表中选择物流公司';
    }
    if (required && !selectedValue) {
        return '请选择物流公司';
    }
    return '';
}

function createEmptyExpressItem() {
    return { no: '' };
}

function normalizeExpressNoList(source) {
    let nos = [];
    if (Array.isArray(source)) {
        nos = source
            .map((item) => (item && typeof item === 'object' ? item.no : item))
            .map((s) => String(s || '').trim())
            .filter(Boolean);
    } else if (source && typeof source === 'object') {
        const raw = source.expressNos || source.logisticsNos;
        if (Array.isArray(raw)) {
            nos = raw.map((s) => String(s).trim()).filter(Boolean);
        }
    }
    return nos.length ? nos.map((no) => ({ no })) : [createEmptyExpressItem()];
}

function collectExpressNos(list) {
    const seen = new Set();
    const out = [];
    (list || []).forEach((item) => {
        const no = (item && item.no ? String(item.no) : '').trim();
        if (no && !seen.has(no)) {
            seen.add(no);
            out.push(no);
        }
    });
    return out;
}

function formatExpressNosDisplay(source) {
    let nos = [];
    if (Array.isArray(source)) {
        nos = source.map((s) => String(s).trim()).filter(Boolean);
    } else if (source && typeof source === 'object') {
        const raw = source.expressNos || source.logisticsNos;
        if (Array.isArray(raw)) {
            nos = raw.map((s) => String(s).trim()).filter(Boolean);
        }
    }
    const unique = [...new Set(nos)];
    return unique.length ? unique.join('、') : '';
}

function validateExpressNoList(list, emptyMessage) {
    const nos = collectExpressNos(list);
    if (!nos.length) {
        return emptyMessage || '请填写快递单号';
    }
    return '';
}

module.exports = {
    isUnauthorizedError,
    formatSenderLabel,
    buildSenderOptions,
    buildSenderDisplay,
    buildSenderViewState,
    getSenderSelectionAfterDelete,
    buildSenderStateAfterDelete,
    buildOpenSenderListPopupState,
    buildCloseSenderListPopupState,
    buildOpenSenderModalState,
    buildCloseSenderModalState,
    buildSenderSelectState,
    validateSenderInput,
    normalizeLogisticsSuggestList,
    validateLogisticsSelected,
    createEmptyExpressItem,
    normalizeExpressNoList,
    collectExpressNos,
    formatExpressNosDisplay,
    validateExpressNoList,
};
