package org.mifos.mobilewallet.core.domain.usecase;

import org.mifos.mobilewallet.core.base.UseCase;
import org.mifos.mobilewallet.core.data.fineract.entity.client.ClientAccounts;
import org.mifos.mobilewallet.core.data.fineract.entity.mapper.AccountMapper;
import org.mifos.mobilewallet.core.data.fineract.repository.FineractRepository;
import org.mifos.mobilewallet.core.domain.model.Account;
import org.mifos.mobilewallet.core.utils.Constants;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by naman on 29/8/17.
 */

public class FetchWallet extends UseCase<FetchWallet.RequestValues,
        FetchWallet.ResponseValue> {

    private final FineractRepository fineractRepository;

    @Inject
    AccountMapper accountMapper;

    @Inject
    public FetchWallet(FineractRepository fineractRepository) {
        this.fineractRepository = fineractRepository;
    }


    @Override
    protected void executeUseCase(FetchWallet.RequestValues requestValues) {

        fineractRepository.getSelfAccounts(requestValues.clientId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ClientAccounts>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getUseCaseCallback().onError("Error fetching accounts");
                    }

                    @Override
                    public void onNext(ClientAccounts clientAccounts) {
                        List<Account> accounts = accountMapper.transform(clientAccounts);
                        if (accounts != null && accounts.size() != 0) {
                            Account walletAccount = null;
                            for (Account account : accounts) {
                                if (account.getProductId() ==
                                        Constants.WALLET_ACCOUNT_SAVINGS_PRODUCT_ID) {
                                    walletAccount = account;
                                    break;
                                }
                            }
                            if (walletAccount != null) {
                                getUseCaseCallback().onSuccess(new ResponseValue(walletAccount));
                            } else {
                                getUseCaseCallback().onError("No wallet account found");
                            }
                        } else {
                            getUseCaseCallback().onError("Error fetching wallet account");
                        }
                    }
                });
    }


    public static final class RequestValues implements UseCase.RequestValues {

        private final long clientId;

        public RequestValues(long clientId) {
            this.clientId = clientId;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final Account walletAccount;

        public ResponseValue(Account account) {
            this.walletAccount = account;
        }

        public Account getWalletAccount() {
            return walletAccount;
        }
    }
}

