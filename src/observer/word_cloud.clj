(ns observer.word-cloud
  (:import (com.kennycason.kumo WordFrequency WordCloud CollisionMode)
           (com.kennycason.kumo.bg CircleBackground PixelBoundaryBackground RectangleBackground)
           (com.kennycason.kumo.font.scale LinearFontScalar SqrtFontScalar)
           (com.kennycason.kumo.image AngleGenerator)
           (com.kennycason.kumo.palette ColorPalette)
           (com.kennycason.kumo.wordstart CenterWordStart)
           (java.awt Color Dimension)))

(def word-frequencies
  (map (fn [{:keys [word frequency]}]
         (WordFrequency. word frequency))
       (apply concat
         (repeat
           8
           [{:word "ukraine", :frequency 5}
            {:word "deep", :frequency 5}
            {:word "weapon", :frequency 5}
            {:word "strike", :frequency 5}
            {:word "russia", :frequency 5}
            {:word "long", :frequency 5}
            {:word "range", :frequency 5}
            {:word "day", :frequency 4}
            {:word "inside", :frequency 4}
            {:word "developed", :frequency 4}
            {:word "says", :frequency 4}
            {:word "zelenskyy", :frequency 3}]))))

(defn create []
  (let [dimension (Dimension. 1080 566)
        word-cloud (WordCloud. dimension CollisionMode/RECTANGLE)]
    (.setPadding word-cloud 5)
    (.setBackground word-cloud (RectangleBackground. dimension))
    (.setBackgroundColor word-cloud Color/white)
    ;(.setColorPalette word-cloud (ColorPalette 20))
    ;(.setWordStartStrategy word-cloud (CenterWordStart.))
    (.setFontScalar word-cloud (SqrtFontScalar. 5 20))
    (.setAngleGenerator word-cloud (AngleGenerator. -10 10 5))
    (.build word-cloud word-frequencies)
    (.writeToFile word-cloud "wordcloud_rectangle.png")))
