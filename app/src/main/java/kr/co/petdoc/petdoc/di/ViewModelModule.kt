package kr.co.petdoc.petdoc.di

import kr.co.petdoc.petdoc.fragment.mypage.health_care.MyHeathCareResultConfig
import kr.co.petdoc.petdoc.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        ExternalDeviceDetailViewModel(get())
    }

    viewModel {
        FirmwareUpgradeViewModel(get(), get())
    }

    viewModel {
        (category: String) -> AllergeCommentViewModel(get(), category)
    }

    viewModel {
        (config: MyHeathCareResultConfig) -> MyHealthCareResultViewModel(config, get())
    }

    viewModel {
        MyPostViewModel(get())
    }

    viewModel {
        MyAlarmViewModel(get())
    }

    viewModel {
        (category:String) -> BloodCommentViewModel(get(), category)
    }

    viewModel {
        PetCheckAdvancedPurchaseViewModel(get(), get(), get())
    }

    viewModel {
        MyPurchaseHistoryModel(get(), get())
    }

    viewModel {
        (orderId: String) -> MyPurchaseDetailViewModel(get(), get(), orderId)
    }

    viewModel {
        (orderId: String) -> MyPurchaseCancelViewModel(get(), get(), orderId)
    }
}