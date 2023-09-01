(ns observer.word-cloud
  (:require [observer.fs :as fs])
  (:import (com.kennycason.kumo WordFrequency WordCloud CollisionMode)
           (com.kennycason.kumo.bg PixelBoundaryBackground)
           (com.kennycason.kumo.font.scale LinearFontScalar)
           (com.kennycason.kumo.image AngleGenerator)
           (com.kennycason.kumo.palette ColorPalette)
           (java.awt Color Dimension)
           (java.awt.image BufferedImage)
           (java.io File)
           (javax.imageio ImageIO)))

(def width 1080)
(def height 607)

(def background-color
  (Color. 255 255 255))

(def color-palette
  (map
    #(Color. %)
    [0x97C2FC
     0xFB7E81
     0x7BE141
     0xEB7DF4
     0xAD85E4
     0xFFA807
     0x6E6EFD
     0xFFC0CB
     0xC2FABC
     0xEE0000
     0xFF6000
     0x2B7CE9
     0x255C03
     0xFF007E
     0x7C29F0
     0xFD5A77
     0x74D66A
     0x990000]))

(defn- rand-offset []
  (* 30 (- 0.5 (rand))))

(defn- stop-words []
  (->> ["i" "me" "my" "myself" "we" "our" "ours" "ourselves" "you" "your" "yours" "yourself" "yourselves" "he" "him"
        "his" "himself" "she" "her" "hers" "herself" "it" "its" "itself" "they" "them" "their" "theirs" "themselves"
        "what" "which" "who" "whom" "this" "that" "these" "those" "am" "is" "are" "was" "were" "be" "been" "being"
        "have" "has" "had" "having" "do" "does" "did" "doing" "a" "an" "the" "and" "but" "if" "or" "because" "as"
        "until" "while" "of" "at" "by" "for" "with" "about" "against" "between" "into" "through" "during" "before"
        "after" "above" "below" "to" "from" "up" "down" "in" "out" "on" "off" "over" "under" "again" "further" "then"
        "once" "here" "there" "when" "where" "why" "how" "all" "any" "both" "each" "few" "more" "most" "other" "some"
        "such" "no" "nor" "not" "only" "own" "same" "so" "than" "too" "very" "can" "will" "just" "don" "should" "now"]
       (repeat 5)
       (apply concat)
       shuffle
       (map
         #(WordFrequency.
            % (inc (rand-int 2))))))

(defn- word-frequencies [selected-context-keywords]
  (concat
    (->> selected-context-keywords
         (mapcat
           (fn [{:keys [keyword agencies]}]
             (repeatedly
               agencies
               #(WordFrequency.
                  keyword
                  (+ 10 (rand-int 10))))))
         shuffle)
    (stop-words)))

(defn- save-cloud-image [output-path]
  (let [image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (.createGraphics image)
        number-of-circles 16
        pad-ratio 0.25
        width-pad (* width pad-ratio)
        height-pad (* height pad-ratio)
        init-width-oval (- width width-pad width-pad)
        init-height-oval (- height height-pad height-pad)
        h (+ width-pad (/ init-width-oval 2))
        k (+ height-pad (/ init-height-oval 2))
        a (/ init-width-oval 2)
        b (/ init-height-oval 2)
        small-oval-ratio 0.3]
    (.setColor g background-color)
    (.fillOval g width-pad height-pad init-width-oval init-height-oval)
    (doseq [_ (range 1 number-of-circles)]
      (let [t (* 2 Math/PI (rand))
            x-center (+ h (* a (Math/cos t)))
            y-center (+ k (* b (Math/sin t)))
            small-oval-width (+ (* width small-oval-ratio) (rand-offset))
            small-oval-height (+ (* height small-oval-ratio) (rand-offset))
            x (- x-center (/ small-oval-width 2) (rand-offset))
            y (- y-center (/ small-oval-height 2) (rand-offset))]
        (.fillOval g x y small-oval-width small-oval-height)))
    (.dispose g)
    (ImageIO/write image "PNG" (File. ^String output-path))))

(defn- save-word-cloud-image
  [cloud-path output-path selected-context-keywords]
  (let [dimension (Dimension. width height)
        word-cloud (WordCloud. dimension CollisionMode/PIXEL_PERFECT)]
    (.setPadding word-cloud 5)
    (.setBackground word-cloud (PixelBoundaryBackground. ^String cloud-path))
    (.setBackgroundColor word-cloud background-color)
    (.setColorPalette word-cloud (ColorPalette. color-palette))
    (.setFontScalar word-cloud (LinearFontScalar. 5 50))
    (.setAngleGenerator word-cloud (AngleGenerator. -10 10 10))
    (.build word-cloud (word-frequencies selected-context-keywords))
    (.writeToFile word-cloud output-path)))

(defn create [selected-context-keywords]
  (let [cloud-path (fs/random-abs-path "png")
        word-cloud-path (fs/random-abs-path "png")]
    (save-cloud-image cloud-path)
    (save-word-cloud-image
      cloud-path
      word-cloud-path
      selected-context-keywords)
    word-cloud-path))
