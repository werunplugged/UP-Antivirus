package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.account.SessionData
import com.unplugged.account.UpAccount

class GetSessionUseCase {

    operator fun invoke(): SessionData? {
        return UpAccount.getSession()
    }
}