package com.maxvision.tech.robot.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.maxvision.tech.robot.R;

/**
 * User: wy
 * Date: 2018/12/12
 * Time: 11:18
 */
public class ImageDialogFragment extends DialogFragment {

    public static ImageDialogFragment getInstance(String imageUrl) {
        ImageDialogFragment imageDialogFragment = new ImageDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("image", imageUrl);
        imageDialogFragment.setArguments(bundle);
        return imageDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_image_head, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PhotoView head = view.findViewById(R.id.big_user_head);
        Bundle bundle = getArguments();
        if (bundle != null) {
            Glide.with(this).load(bundle.getString("image")).into(head);
        }
        view.setOnClickListener(v -> ImageDialogFragment.this.dismiss());
        head.setOnClickListener(v -> ImageDialogFragment.this.dismiss());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), R.style.ImageDialog);
    }
}
