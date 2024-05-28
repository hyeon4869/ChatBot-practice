
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import UserUserManager from "./components/listers/UserUserCards"
import UserUserDetail from "./components/listers/UserUserDetail"

import ChatBotChatBotManager from "./components/listers/ChatBotChatBotCards"
import ChatBotChatBotDetail from "./components/listers/ChatBotChatBotDetail"


export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/users/users',
                name: 'UserUserManager',
                component: UserUserManager
            },
            {
                path: '/users/users/:id',
                name: 'UserUserDetail',
                component: UserUserDetail
            },

            {
                path: '/chatBots/chatBots',
                name: 'ChatBotChatBotManager',
                component: ChatBotChatBotManager
            },
            {
                path: '/chatBots/chatBots/:id',
                name: 'ChatBotChatBotDetail',
                component: ChatBotChatBotDetail
            },



    ]
})
