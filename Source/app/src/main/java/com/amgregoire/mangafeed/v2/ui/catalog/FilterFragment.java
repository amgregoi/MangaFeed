//package com.amgregoire.mangafeed.v2.ui.catalog;
//
//package com.amgregoire.mangafeed.UI.Fragments.Base;
//
//import android.arch.lifecycle.ViewModelProviders;
//import android.support.constraint.ConstraintLayout;
//import android.support.design.widget.BottomSheetBehavior;
//import android.support.v7.widget.SearchView;
//import android.view.View;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.amgregoire.mangafeed.Common.MangaEnums;
//import com.amgregoire.mangafeed.FilterViewModel;
//import com.amgregoire.mangafeed.R;
//import com.amgregoire.mangafeed.UI.Mappers.IFilter;
//import com.amgregoire.mangafeed.UI.Presenters.Base.FilterPres;
//import com.amgregoire.mangafeed.Utils.KeyboardUtil;
//
//import butterknife.BindView;
//import butterknife.OnClick;
//
///**
// * Created by amgregoi on 2/5/19.
// */
//
//public class FilterFragment extends BackPressFragment implements IFilter
//{
//    @BindView(R.id.constraintLayoutBottomSheet) protected ConstraintLayout mBottomSheet;
//    @BindView(R.id.viewCover) View mViewCover;
//    @BindView(R.id.bottomSheetHeader) protected TextView mBottomHeader;
//    @BindView(R.id.buttonFilterStatus) ImageButton mBottomHeaderButton;
//    @BindView(R.id.searchView) protected SearchView mSearchView;
//    @BindView(R.id.viewFilterCompleteCover) View mCompleteFilter;
//    @BindView(R.id.viewFilterReadingCover) View mReadingFilter;
//
//    @OnClick(R.id.bottomSheetHeader)
//    public void onBottomSheetHeaderSelected()
//    {
//        boolean isCollapsed = BottomSheetBehavior.from(mBottomSheet)
//                                                 .getState() == BottomSheetBehavior.STATE_COLLAPSED;
//        mPresenter.onBottomSheetHeaderSelected(isCollapsed);
//    }
//
//    @OnClick(R.id.viewCover)
//    public void onViewCoverSelected()
//    {
//        mPresenter.onViewCoverSelected();
//    }
//
//    @OnClick(R.id.buttonFilterStatus)
//    public void onFilterStatusButtonSelected()
//    {
//        boolean isCollapsed = BottomSheetBehavior.from(mBottomSheet)
//                                                 .getState() == BottomSheetBehavior.STATE_COLLAPSED;
//        mPresenter.onHeaderButtonSelected(isCollapsed);
//    }
//
//    @OnClick(R.id.viewFilterReadingCover)
//    public void onReadingFilterSelected()
//    {
//        mPresenter.onFilterReadingSelected();
//    }
//
//    @OnClick(R.id.viewFilterCompleteCover)
//    public void onCompleteFilterSelected()
//    {
//        mPresenter.onFilterCompleteSelected();
//    }
//
//    @Override
//    public void setupSearchView()
//    {
//        ImageView searchClose = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
//        searchClose.setImageDrawable(null);
//
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
//        {
//            @Override
//            public boolean onQueryTextSubmit(String s)
//            {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String filter)
//            {
//                mPresenter.onSearchQueryUpdated(filter);
//                return false;
//            }
//        });
//    }
//
//    @Override
//    public void setupViewModels()
//    {
//        if (getActivity() != null)
//        {
//            ViewModelProviders.of(getActivity())
//                              .get(FilterViewModel.class)
//                              .getTextFilter()
//                              .observe(getActivity(), filterModel ->
//                              {
//                                  boolean isCollapsed = BottomSheetBehavior.from(mBottomSheet)
//                                                                           .getState() == BottomSheetBehavior.STATE_COLLAPSED;
//                                  mPresenter.onFilterViewModelUpdated(filterModel, isCollapsed);
//                              });
//        }
//    }
//
//    @Override
//    public void setBottomSheetCollapsed()
//    {
////        hideKeyboard();
//        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        mSearchView.clearFocus();
//    }
//
//    @Override
//    public void setBottomSheetExpanded()
//    {
//        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//    }
//
//    @Override
//    public void showCoverFilter()
//    {
//        mViewCover.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void hideCoverFilter()
//    {
//        mViewCover.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void setSearchQuery(String query)
//    {
//        mSearchView.setQuery(query, false);
//    }
//
//    @Override
//    public void setBottomSheetButtonClose()
//    {
//        mBottomHeaderButton.setImageDrawable(getResources().getDrawable(R.drawable.close_white));
//    }
//
//    @Override
//    public void setBottomSheetButtonUp()
//    {
//        mBottomHeaderButton.setImageDrawable(getResources().getDrawable(R.drawable.carrot_up));
//    }
//
//    @Override
//    public void setBottomSheetButtonDown()
//    {
//        mBottomHeaderButton.setImageDrawable(getResources().getDrawable(R.drawable.carrot_down));
//    }
//
//    @Override
//    public void setBottomSheetHeaderAddFilter()
//    {
//        mBottomHeader.setText(R.string.add_filter);
//    }
//
//    @Override
//    public void setBottomSheetHeaderClearFilter()
//    {
//        mBottomHeader.setText(R.string.clear_filters);
//    }
//
//    @Override
//    public void hideKeyboard()
//    {
//        if (getActivity() != null)
//        {
//            KeyboardUtil.hide(getActivity());
//        }
//    }
//
//    @Override
//    public void updateFilterViewModel(String queryFilter, MangaEnums.FilterStatus statusFilter)
//    {
//        if (getActivity() != null)
//        {
//            ViewModelProviders.of(getActivity())
//                              .get(FilterViewModel.class)
//                              .setTextFilter(queryFilter, statusFilter);
//        }
//    }
//
//    @Override
//    public void clearStatusFilterBackgrounds()
//    {
//        mReadingFilter.setBackground(null);
//        mCompleteFilter.setBackground(null);
//    }
//
//    @Override
//    public void setStatusFilterReading()
//    {
//        mReadingFilter.setBackgroundColor(getResources().getColor(R.color.colorAccentTransparent));
//    }
//
//    @Override
//    public void setStatusFilterComplete()
//    {
//        mCompleteFilter.setBackgroundColor(getResources().getColor(R.color.colorAccentTransparent));
//    }
//
//    @Override
//    public void setStatusFilterOnHold()
//    {
//
//    }
//
//    @Override
//    public void setStatusFilterPlanToRead()
//    {
//
//    }
//}
