package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUserByUserName(username: String): User? {
        return userDao.getUserByUsername(username)
    }


    }


