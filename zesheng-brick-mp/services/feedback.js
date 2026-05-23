const { post, get } = require('../utils/request');

function submitFeedback(payload) {
    return post('/feedback', payload);
}

function getMyFeedbackPage(params) {
    return get('/feedback/my-page', params);
}

module.exports = {
    submitFeedback,
    getMyFeedbackPage
};
