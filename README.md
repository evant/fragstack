# fragstack
A better fragment back-stack™

The major difference from the built-in fragment back-stack is that
fragments in the back-stack on completely destroyed, not just their views. This removes the weird
extra `onViewCreated(View, Bundle)`/`Fragment#onDestroyView()` lifecycle
that fragments have.

## Usage

Specify the intial fragment, then use `push()` and `pop` to manipulate the back-stack.

```kotlin
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import me.tatarka.fragstack.ktx.backStack

class MainActivity : AppCompatActivity {
  override fun onCreate(savedInstanceState: Bundle?) {
    backStack.startWith(R.id.content, DashboardFragment.newInstance())
  }
  
  override fun onBackPressed() {
    if (!backStack.popImmediate()) {
      super.onBackPressed()
    }
  }
}

...

backStack.push(DetailFragment.newInstance())
backStack.pop()
```

Transactions will automatically be optimised, so you are free to edit the
back-stack with multiple operations. For example, to go from [A, B, C] to [A, B, D] you can do

```kotlin
backStack.pop().push(fragmentD)
```

### From Java

Get an instance of the backstack with `FragmentBackstack.of(activity.getSupportFragmentManager())` or
`FragmentBackstack.of(fragment.getChildFragmentManager())`.

## Differences with Fragment Transitions

Unfortunatly the fragment transitions api is tied deeply with the built-in back-stack. It will see a pop from
this lib the same way as a push. This means pop animations will run in the new fragment instead of the old one.
Methods like `setSharedElementReturnTransition()`, `setReenterTransition()` and `setReturnTransition()` will 
be ignored.
