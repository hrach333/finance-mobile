package com.hrach.financeapp.data.repository

interface GroupMemberMutationsRepository {
    suspend fun addGroupMember(groupId: Int, email: String, role: String)
    suspend fun updateGroupMemberRole(groupId: Int, memberId: Int, role: String)
    suspend fun deleteGroupMember(groupId: Int, memberId: Int)
}
