package org.chimeramc.launcher.launcher.controller;

import org.chimeramc.launcher.R;

public final class ControllerCatalog {
    private ControllerCatalog(){}

    public static final class Label {
        public final String text;
        public final float cx, cy;
        public final boolean above;
        public Label(String text, float cx, float cy, boolean above){
            this.text = text;
            this.cx = cx;
            this.cy = cy;
            this.above = above;
        }
    }

    public static final class Spec {
        public final int baseRes;
        public final ControllerIllustration.Region[] regions;
        public final Label[] labels;
        public Spec(int baseRes, ControllerIllustration.Region[] regions, Label[] labels){
            this.baseRes = baseRes;
            this.regions = regions;
            this.labels = labels;
        }
    }

    public static Spec specFor(ControllerType type){
        switch (type){
            case XBOX: return xboxSpec();
            case DS4: return ds4Spec();
            case DUAL_SENSE: return dualSenseSpec();
            default: return xboxSpec();
        }
    }

    private static Spec xboxSpec(){
        ControllerIllustration.Shape c = ControllerIllustration.Shape.CIRCLE;
        ControllerIllustration.Shape r = ControllerIllustration.Shape.ROUND_RECT;
        ControllerIllustration.Region[] regs = new ControllerIllustration.Region[]{
            new ControllerIllustration.Region("LT", 80f, 44f, 21f, 4f, r),
            new ControllerIllustration.Region("LB", 82f, 67f, 24f,  7f, r),
            new ControllerIllustration.Region("RB", 218f, 67f,  24f, 7f, r),
            new ControllerIllustration.Region("RT", 220f, 44f,  21f,  4f, r),
            new ControllerIllustration.Region("LS", 105f, 110f,  20f,  20f, c),
            new ControllerIllustration.Region("RS", 215f, 110f,  20f,  20f, c),
            new ControllerIllustration.Region("DPAD", 91f, 110f, 18f,  12f, r),
            new ControllerIllustration.Region("A", 205f, 118f,  11f,  11f, c),
            new ControllerIllustration.Region("B", 228f, 95f,  11f,  11f, c),
            new ControllerIllustration.Region("X", 182f, 95f,  11f,  11f, c),
            new ControllerIllustration.Region("Y", 205f,  72f, 11f,  11f, c),
            new ControllerIllustration.Region("VIEW", 153f, 89f,  9f,  9f, r),
            new ControllerIllustration.Region("MENU", 171f, 089f,  9f,  9f, r),
            new ControllerIllustration.Region("XBOX", 160f, 104f,  9f,  9f, c),
        };
        ControllerCatalog.Label[] labs = new ControllerCatalog.Label[]{
            new Label("LT", 80f, 30f, true),
            new Label("RT", 220f, 30f, true),
            new Label("LB", 82f, 54f, true),
            new Label("RB", 218f, 54f, true),
            new Label("L", 78f, 138f, false),
            new Label("R", 252f, 138f, false),
            new Label("DPAD", 91f, 135f, false),
            new Label("A", 205f, 131f, false),
            new Label("B", 239f, 093f, false),
            new Label("X", 184f, 093f, false),
            new Label("Y", 205f, 058f, true),
            new Label("VIEW", 153f, 075f, true),
            new Label("MENU", 171f, 075f, true),
            new Label("XBOX", 176f, 106f, false),
        };
        return new Spec(R.drawable.ic_controller_xbox_base, regs, labs);
    }

    private static Spec ds4Spec(){
        ControllerIllustration.Shape c = ControllerIllustration.Shape.CIRCLE;
        ControllerIllustration.Shape r = ControllerIllustration.Shape.ROUND_RECT;
        ControllerIllustration.Region[] regs = new ControllerIllustration.Region[]{
            new ControllerIllustration.Region("LT", 80f, 44f, 21f, 4f, r),
            new ControllerIllustration.Region("LB", 82f, 67f, 24f, 7f, r),
            new ControllerIllustration.Region("RB", 218f, 67f,  24f,  7f, r),
            new ControllerIllustration.Region("RT", 220f, 44f, 21f, 4f, r),
            new ControllerIllustration.Region("TP", 160f, 93f, 42f,  33f, r),
            new ControllerIllustration.Region("LS", 105f, 110f,  20f,  20f, c),
            new ControllerIllustration.Region("RS", 215f, 110f,  20f,  20f, c),
            new ControllerIllustration.Region("DPAD", 91f, 110f,  18f,  12f, r),
            new ControllerIllustration.Region("A", 248f, 127f,  11f,  11f, c),
            new ControllerIllustration.Region("B", 271f, 104f,  11f,  11f, c),
            new ControllerIllustration.Region("X", 225f, 104f,  11f,  11f, c),
            new ControllerIllustration.Region("Y", 248f, 081f,  11f,  11f, c),
            new ControllerIllustration.Region("PS", 154f, 104f,  9f,  9f, c),
        };
        ControllerCatalog.Label[] labs = new ControllerCatalog.Label[]{
            new Label("LT", 80f, 30f, true),
            new Label("RT", 220f, 30f, true),
            new Label("LB", 82f, 54f, true),
            new Label("RB", 218f, 54f, true),
            new Label("L", 78f, 138f, false),
            new Label("R", 252f, 138f, false),
            new Label("DPAD", 91f, 135f, false),
            new Label("TOUCHPAD", 160f, 50f, true),
            new Label("A", 248f, 140f, false),
            new Label("B", 282f, 102f, false),
            new Label("X", 236f, 102f, false),
            new Label("Y", 248f, 67f, true),
            new Label("PS", 154f, 120f, true),
        };
        return new Spec(R.drawable.ic_controller_ds4_base, regs, labs);
    }

    private static Spec dualSenseSpec(){
        ControllerIllustration.Shape c = ControllerIllustration.Shape.CIRCLE;
        ControllerIllustration.Shape r = ControllerIllustration.Shape.ROUND_RECT;
        ControllerIllustration.Region[] regs = new ControllerIllustration.Region[]{
            new ControllerIllustration.Region("LT", 80f, 44f, 21f, 4f, r),
            new ControllerIllustration.Region("LB", 82f, 67f, 24f, 7f, r),
            new ControllerIllustration.Region("RB", 218f, 67f, 24f,  7f, r),
            new ControllerIllustration.Region("RT", 220f, 44f,  21f,  4f, r),
            new ControllerIllustration.Region("TP", 155f,  99f,  42f,  33f, r),
            new ControllerIllustration.Region("LS", 105f, 110f,  20f,  20f, c),
            new ControllerIllustration.Region("RS", 215f, 110f,  20f,  20f, c),
            new ControllerIllustration.Region("DPAD", 91f, 110f,  18f,  12f, r),
            new ControllerIllustration.Region("A", 252f, 125f,  11f,  11f, c),
            new ControllerIllustration.Region("B", 275f, 102f,  11f,  11f, c),
            new ControllerIllustration.Region("X", 229f, 102f,  11f,  11f, c),
            new ControllerIllustration.Region("Y", 252f, 079f,  11f,  11f, c),
            new ControllerIllustration.Region("CREATE", 107f,  99f,  9f,  9f, r),
            new ControllerIllustration.Region("OPTIONS", 203f,  99f,  9f,  9f, r),
            new ControllerIllustration.Region("PS", 154f,  146f,  9f,  9f, c),
        };
        ControllerCatalog.Label[] labs = new ControllerCatalog.Label[]{
            new Label("LT", 80f, 30f, true),
            new Label("RT", 220f, 30f, true),
            new Label("LB", 82f, 54f, true),
            new Label("RB", 218f, 54f, true),
            new Label("L", 78f, 138f, false),
            new Label("R", 252f, 138f, false),
            new Label("DPAD", 91f, 135f, false),
            new Label("TOUCHPAD", 155f, 52f, true),
            new Label("CREATE", 107f, 118f, false),
            new Label("OPTIONS", 203f, 118f, false),
            new Label("A", 252f, 138f, false),
            new Label("B", 286f, 100f, false),
            new Label("X", 240f, 100f, false),
            new Label("Y", 252f, 65f, true),
            new Label("PS", 154f, 162f, false),
        };
        return new Spec(R.drawable.ic_controller_dualsense_base, regs, labs);
    }
}
