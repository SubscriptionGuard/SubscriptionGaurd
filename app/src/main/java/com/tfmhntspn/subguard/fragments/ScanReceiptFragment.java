
package com.tfmhntspn.subguard.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentScanReceiptBinding;

import java.io.IOException;
import java.util.List;

public class ScanReceiptFragment extends Fragment {

    private FragmentScanReceiptBinding binding;

    // Receives the image returned by Google's document scanner
    private final ActivityResultLauncher<IntentSenderRequest> scannerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {

                        if (result.getResultCode() != Activity.RESULT_OK) {
                            return;
                        }

                        GmsDocumentScanningResult scanResult =
                                GmsDocumentScanningResult
                                        .fromActivityResultIntent(
                                                result.getData()
                                        );

                        if (scanResult == null) {
                            showMessage("No scan result received");
                            return;
                        }

                        List<GmsDocumentScanningResult.Page> pages =
                                scanResult.getPages();

                        if (pages == null || pages.isEmpty()) {
                            showMessage("No receipt image found");
                            return;
                        }

                        // Process the first scanned page
                        Uri imageUri = pages.get(0).getImageUri();

                        recognizeReceiptText(imageUri);
                    }
            );

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentScanReceiptBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCloseScanner.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        // The Google scanner provides its own camera controls
        binding.btnFlash.setOnClickListener(v ->
                showMessage("Use the scanner's camera controls")
        );

        binding.btnCaptureReceipt.setOnClickListener(v ->
                openDocumentScanner()
        );
    }

    private void openDocumentScanner() {

        GmsDocumentScannerOptions options =
                new GmsDocumentScannerOptions.Builder()
                        .setGalleryImportAllowed(true)
                        .setPageLimit(1)
                        .setResultFormats(
                                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                        )
                        .setScannerMode(
                                GmsDocumentScannerOptions.SCANNER_MODE_FULL
                        )
                        .build();

        GmsDocumentScanner scanner =
                GmsDocumentScanning.getClient(options);

        scanner.getStartScanIntent(requireActivity())
                .addOnSuccessListener(intentSender -> {

                    IntentSenderRequest request =
                            new IntentSenderRequest.Builder(
                                    intentSender
                            ).build();

                    scannerLauncher.launch(request);
                })
                .addOnFailureListener(e ->
                        showMessage(
                                "Unable to open scanner: " + e.getMessage()
                        )
                );
    }

    private void recognizeReceiptText(Uri imageUri) {

        if (!isAdded()) return;

        InputImage image;

        try {
            image = InputImage.fromFilePath(
                    requireContext(),
                    imageUri
            );
        } catch (IOException e) {
            showMessage("Unable to read the scanned receipt");
            return;
        }

        TextRecognizer recognizer =
                TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                );

        recognizer.process(image)
                .addOnSuccessListener(result -> {

                    if (!isAdded() || binding == null) return;

                    String extractedText = result.getText();

                    if (extractedText == null
                            || extractedText.trim().isEmpty()) {

                        showMessage(
                                "No text detected. Try scanning again."
                        );
                        return;
                    }

                    // Pass the recognized text to the review screen
                    Bundle args = new Bundle();
                    args.putString("scanned_text", extractedText);

                    NavHostFragment.findNavController(this).navigate(
                            R.id.action_scanReceiptFragment_to_reviewScanDetailsFragment,
                            args
                    );
                })
                .addOnFailureListener(e ->
                        showMessage(
                                "Text recognition failed: " + e.getMessage()
                        )
                )
                .addOnCompleteListener(task ->
                        recognizer.close()
                );
    }

    private void showMessage(String message) {

        if (!isAdded()) return;

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
