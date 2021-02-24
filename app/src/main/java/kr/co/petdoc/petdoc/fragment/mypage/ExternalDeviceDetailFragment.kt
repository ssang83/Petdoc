package kr.co.petdoc.petdoc.fragment.mypage

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_external_device_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.scanner.startUpgrade
import kr.co.petdoc.petdoc.scanner.uploadFirmware
import kr.co.petdoc.petdoc.utils.DownloadFileAsync
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import java.io.File

/**
 * Petdoc
 * Class: ExternalDeviceDetailFragment
 * Created by kimjoonsung on 2020/07/02.
 *
 * Description :
 */

class ExternalDeviceDetailFragment : BaseFragment() {

    private val MY_PERMISSION_STORAGE = 1111

    private var seqFirmwareUpgrade = 0
    private var downloadFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_external_device_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val device = arguments?.getString("name") ?:""

        title.text = device

        btnBack.setOnSingleClickListener { requireActivity().onBackPressed() }

        layoutDevice.setOnSingleClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle("기기 삭제")
                setMessage("사용자의 기기가 이 Wi-Fi 네트워크에\n더 이상 연결되지 않습니다.")
                setConfirmButton("삭제", View.OnClickListener {
                    StorageUtils.writeValueInPreference(
                        requireContext(),
                        AppConstants.PREF_KEY_PETDOC_SCAN_NAME,
                        ""
                    )

                    val scanner: Scanner by inject()
                    scanner.disconnect()

                    dismiss()
                    requireActivity().onBackPressed()
                })
                setCancelButton("취소", View.OnClickListener {
                    dismiss()
                })
            }.show()
        }

    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of request.
     *
     * @param response response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {

    }

    //===============================================================================================
    /**
     * 스캐너 펌웨어를 다운로드 한다.
     */
    private fun downloadFirmware(url: String) {
        val downloadPath = "${requireActivity().externalCacheDir}/Petdoc/Scanner/SYSUPGRADE/"

        DownloadFileAsync("$downloadPath/firmware.bin", object : DownloadFileAsync.PostDownload {
            override fun downloadDone(file: File?) {
                if (file != null && file.length() > 0) {
                    downloadFile = file
                    TwoBtnDialog(requireContext()).apply {
                        setTitle("펌웨어 버전 확인")
                        setMessage("신규 펌웨어가 존재합니다. 업데이트를 하시겠습니까?\n충전기가 연결된 상태에서 업데이트를 진행 할 수 있습니다.")
                        setConfirmButton("확인", View.OnClickListener {
                            updateFirmware()
                            dismiss()
                        })
                        setCancelButton("취소", View.OnClickListener {
                            dismiss()
                        })
                    }.show()
                }
            }
        }).execute(url)
    }

    /**
     * 스캐너 펌웨어를 업데이트 한다.
     */
    private fun updateFirmware() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (StorageUtils.loadIntValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_SCANNER_ADAPTER_STATUS
                ) == 4
            ) {
                launch(Dispatchers.Main) { firmwareWatchdog() }
                seqFirmwareUpgrade = uploadFirmware("")
                seqFirmwareUpgrade = startUpgrade()
            } else {
                launch(Dispatchers.Main) {
                    OneBtnDialog(requireContext()).apply {
                        setTitle("펌웨어 업데이트")
                        setMessage("충전기가 연결된 상태에서만 업그레이드가 가능합니다.")
                        setConfirmButton(
                            Helper.readStringRes(R.string.confirm),
                            View.OnClickListener {
                                dismiss()
                            })
                        show()
                    }
                }
            }
        }
    }

    /**
     * 스캐너 펌웨어 업데이트 상태 체크한다.
     */
    private fun firmwareWatchdog() {
        while (true) {
            if (seqFirmwareUpgrade == 0) {
                Logger.d("[1/3] 펌웨어 전송 중")
            } else if (seqFirmwareUpgrade == 1) {
                Logger.d("[2/3] 펌웨어 업데이트 중")
            } else {
                Logger.d("[3/3] 펌웨어 업데이트 완료")
                downloadFile?.delete()

                OneBtnDialog(requireContext()).apply {
                    setTitle("펌웨어 업데이트")
                    setMessage("펌웨어 업그레이드 완료되었습니다.\n진단기가 재부팅 됩니다.")
                    setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                        dismiss()
                    })
                    show()
                }
                break
            }
        }
    }
}

/*
TODO. 펌웨어 업데이트 작업 내용 (기능 적용시 아래 주석 코드로 적용)
class ExternalDeviceDetailFragment
    : PetdocBaseFragment<FragmentExternalDeviceDetailBinding, ExternalDeviceDetailViewModel>() {

    private val viewModel: ExternalDeviceDetailViewModel by viewModel()

    override fun getTargetViewModel(): ExternalDeviceDetailViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_external_device_detail

    private var downloadDialog: FirmwareDownloadDialog? = null
    private var loadingDialog: LoadingDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            start(requireActivity().intent.hasExtra("downloadFirmware"))

            startFirmwareDetailScreen.observe(this@ExternalDeviceDetailFragment, Observer {
                findNavController().navigate(
                    R.id.action_externalDeviceDetailFragment_to_firmwareInstalledVersionInfoFragment
                )
            })
            startUpdateDetailScreen.observe(this@ExternalDeviceDetailFragment, Observer {
                findNavController().navigate(
                    R.id.action_externalDeviceDetailFragment_to_firmwareUpdateInfoFragment
                )
            })
            startInstallScreen.observe(this@ExternalDeviceDetailFragment, Observer {
                // 설치전 스캐너를 연결해야 하기때문에 ReconnectScannerActivity 에 firmwareUpdate extra 전달하여
                // 연결 완료 이후에 FirmwareUpgradeActivity 를 호출
                requireContext().startActivity<ReConnectScannerActivity> {
                    putExtra("firmwareUpdate", true)
                }
                val navController = findNavController()
                if (navController.graph.startDestination == navController.currentDestination?.id) {
                    requireActivity().finish()
                } else {
                    navController.popBackStack(navController.graph.startDestination, false)
                }
            })
            showDeleteDeviceDialog.observe(this@ExternalDeviceDetailFragment, Observer {
                TwoBtnDialog(requireContext()).apply {
                    setTitle("기기 삭제")
                    setMessage("사용자의 기기가 이 Wi-Fi 네트워크에\n더 이상 연결되지 않습니다.")
                    setConfirmButton("취소", View.OnClickListener {
                        dismiss()
                    })
                    setCancelButton("삭제", View.OnClickListener {
                        StorageUtils.writeValueInPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_PETDOC_SCAN_NAME,
                            ""
                        )

                        StorageUtils.writeBooleanValueInPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS,
                            false
                        )

                        if (PetdocApplication.mLiteCamera != null) {
                            PetdocApplication.mLiteCamera!!.setCameraListener(null)
                            PetdocApplication.mLiteCamera!!.clear()
                            PetdocApplication.mLiteCamera = null
                        }

                        dismiss()
                    })
                }.show()
            })
            firmwareDownloadCompleted.observe(this@ExternalDeviceDetailFragment, Observer {
                if (downloadDialog == null || downloadDialog?.isShowing == false) {
                    showDownloadDialog(FirmwareVersionState.DOWNLOADED)
                } else {
                    downloadDialog?.resetViewByState(FirmwareVersionState.DOWNLOADED)
                }
            })
            showFirmwareDownloadDialog.observe(this@ExternalDeviceDetailFragment,
                Observer { firmwareState ->
                    showDownloadDialog(firmwareState)
                })
            isDownloadProgressShowing.observe(viewLifecycleOwner, Observer { show ->
                if (show) {
                    loadingDialog = LoadingDialog(requireContext())
                    loadingDialog?.show()
                } else {
                    if (loadingDialog?.isShowing == true) {
                        loadingDialog?.dismiss()
                    }
                }
            })
        }

        setUpViews()
    }

    private fun showDownloadDialog(firmwareState: FirmwareVersionState) {
        downloadDialog = FirmwareDownloadDialog(requireContext(), firmwareState).apply {
            setFirmwareDownloadDialogListener(object : FirmwareDownloadDialog.FirmwareDownloadDialogListener {
                override fun onDownloadBtnClicked() {
                    viewModel.onDownloadFirmwareClicked()
                }

                override fun onInstallBtnClicked() {
                    viewModel.onInstallFirmwareClicked()
                    downloadDialog?.dismiss()
                }
            })
        }.also { it.show() }
    }

    private fun setUpViews() {
        btnBack.setOnClickListener {
            val navController = findNavController()
            if (navController.graph.startDestination == navController.currentDestination?.id) {
                requireActivity().finish()
            } else {
                requireActivity().onBackPressed()
            }
        }

        title.text = if (arguments?.getString("name").isNullOrEmpty()) {
            StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_PETDOC_SCAN_NAME,
                ""
            )
        } else {
            arguments?.getString("name")
        }
    }
}*/
