const { addSender, deleteSender } = require('./sender');
const { isUnauthorizedError } = require('../utils/order-form-shared');

function readSenderListCache(storageKey) {
    try {
        const list = wx.getStorageSync(storageKey);
        return Array.isArray(list) ? list : [];
    } catch (error) {
        return [];
    }
}

function writeSenderListCache(storageKey, senderList) {
    try {
        wx.setStorageSync(storageKey, Array.isArray(senderList) ? senderList : []);
    } catch (error) {
        // ignore
    }
}

function resolveSenderFallbackByError(error, storageKey) {
    if (isUnauthorizedError(error)) {
        return {
            senderList: [],
            unauthorized: true,
        };
    }
    return {
        senderList: readSenderListCache(storageKey),
        unauthorized: false,
    };
}

async function addSenderWithFallback(storageKey, currentList, senderPayload) {
    const senderList = Array.isArray(currentList) ? currentList.slice() : [];
    try {
        await addSender(senderPayload);
        return {
            success: true,
            senderList: null,
        };
    } catch (error) {
        const fallbackList = senderList.concat({
            name: senderPayload.name,
            phone: senderPayload.phone,
            address: senderPayload.address || '',
        });
        writeSenderListCache(storageKey, fallbackList);
        return {
            success: false,
            senderList: fallbackList,
        };
    }
}

async function deleteSenderWithFallback(storageKey, sender, currentList, deleteIndex) {
    const senderList = Array.isArray(currentList) ? currentList.slice() : [];
    if (sender && sender.id != null) {
        await deleteSender(sender.id);
        return {
            remoteDeleted: true,
            senderList: null,
        };
    }
    const fallbackList = senderList.filter((_, index) => index !== deleteIndex);
    writeSenderListCache(storageKey, fallbackList);
    return {
        remoteDeleted: false,
        senderList: fallbackList,
    };
}

module.exports = {
    readSenderListCache,
    writeSenderListCache,
    resolveSenderFallbackByError,
    addSenderWithFallback,
    deleteSenderWithFallback,
};
