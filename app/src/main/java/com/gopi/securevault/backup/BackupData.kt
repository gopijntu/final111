package com.gopi.securevault.backup

import com.gopi.securevault.data.entities.AadharEntity
import com.gopi.securevault.data.entities.BankEntity
import com.gopi.securevault.data.entities.CardEntity
import com.gopi.securevault.data.entities.LicenseEntity
import com.gopi.securevault.data.entities.PanEntity
import com.gopi.securevault.data.entities.PolicyEntity
import com.gopi.securevault.data.entities.VoterIdEntity

data class BackupData(
    val aadhar: List<AadharEntity>,
    val banks: List<BankEntity>,
    val cards: List<CardEntity>,
    val policies: List<PolicyEntity>,
    val pan: List<PanEntity>,
    val voterId: List<VoterIdEntity>,
    val license: List<LicenseEntity>,
    val misc: List<com.gopi.securevault.data.entities.MiscEntity>
)
