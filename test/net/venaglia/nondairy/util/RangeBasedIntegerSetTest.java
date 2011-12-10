/*
 * Copyright 2011 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.venaglia.nondairy.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Performs a 100% code coverage unit test of RangeBasedIntegerSet. :)
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class RangeBasedIntegerSetTest
{
  private static final long TEST_EXPECT_NULL = 3999999999L;
  private static final long TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION = 4000000000L;
  private static final long TEST_EXPECT_ILLEGAL_STATE_EXCEPTION = 4000000001L;
  private static final long TEST_CONCURRENT_MODIFICATION_EXCEPTION = 4000000002L;

  private TestSet getStandardSet1 ()
  {
    TestSet oSet = new TestSet();
    oSet.addAll(10, 19);
    oSet.addAll(30, 39);
    oSet.addAll(50, 59);
    return oSet;
  }

  private TestSet getStandardSet2 ()
  {
    TestSet oSet = new TestSet();
    oSet.addAll(15, 24);
    oSet.addAll(35, 44);
    oSet.addAll(55, 64);
    return oSet;
  }

  private TestSet getStandardSet3 ()
  {
    TestSet oSet = new TestSet();
    oSet.addAll(20, 29);
    oSet.addAll(40, 49);
    oSet.addAll(60, 69);
    return oSet;
  }

  private void check (TestSet oSet, int iSize, String stToString)
  {
    assertEquals("The set did not contain the expected number of elements",
                 iSize,
                 oSet.size());
    assertEquals("The set did not contain the expected ranges",
                 stToString,
                 oSet.toString());
    if (iSize == 0)
    {
      assertTrue("The set identified itself as empty when it was not",
                 oSet.isEmpty());
    }
    else
    {
      assertFalse("The set identified itself as not empty when it was",
                  oSet.isEmpty());
    }
    oSet.assertOptimized();
  }

  @Test
  public void testBasicOperations ()
  {
    TestSet oSet;

    oSet = new TestSet();
    check(oSet, 0, "[]");
    oSet.add(999);
    check(oSet, 1, "[999]");
    oSet.add(1001);
    check(oSet, 2, "[999,1001]");
    oSet.add(1000);
    check(oSet, 3, "[999..1001]");
    oSet.add(1000);
    check(oSet, 3, "[999..1001]");
    oSet.remove(1000);
    check(oSet, 2, "[999,1001]");
    oSet.remove(1000);
    check(oSet, 2, "[999,1001]");
    oSet.add(1000);
    check(oSet, 3, "[999..1001]");
    oSet.remove(999);
    check(oSet, 2, "[1000..1001]");
    oSet.remove(1002);
    check(oSet, 2, "[1000..1001]");
    oSet.add(1001);
    check(oSet, 2, "[1000..1001]");
    oSet.add(1002);
    check(oSet, 3, "[1000..1002]");
    oSet.remove(1002);
    check(oSet, 2, "[1000..1001]");
    oSet.add(999);
    check(oSet, 3, "[999..1001]");
    oSet.remove(999);
    check(oSet, 2, "[1000..1001]");
    oSet.removeAll(999, 1001);
    check(oSet, 0, "[]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.add(24);
    check(oSet, 31, "[10..19,24,30..39,50..59]");
    oSet.addAll(21,27);
    check(oSet, 37, "[10..19,21..27,30..39,50..59]");
    oSet.addAll(20,20);
    check(oSet, 38, "[10..27,30..39,50..59]");
    oSet.removeAll(20,20);
    check(oSet, 37, "[10..19,21..27,30..39,50..59]");
    oSet.add(20);
    check(oSet, 38, "[10..27,30..39,50..59]");
    oSet.addAll(0,100);
    check(oSet, 101, "[0..100]");
    oSet.removeAll(0,9);
    check(oSet, 91, "[10..100]");
    oSet.removeAll(90,99);
    check(oSet, 81, "[10..89,100]");
    oSet.remove(100);
    check(oSet, 80, "[10..89]");
    oSet.removeAll(50,59);
    check(oSet, 70, "[10..49,60..89]");
    oSet.removeAll(60,89);
    check(oSet, 40, "[10..49]");
    oSet.addAll(0,4);
    check(oSet, 45, "[0..4,10..49]");
    oSet.addAll(2,34);
    check(oSet, 50, "[0..49]");

    oSet = getStandardSet2();
    check(oSet, 30, "[15..24,35..44,55..64]");
    oSet.retainAll(0,100);
    check(oSet, 30, "[15..24,35..44,55..64]");
    oSet.addAll(25,54);
    check(oSet, 50, "[15..64]");
    oSet.retainAll(15,64);
    check(oSet, 50, "[15..64]");
    oSet.retainAll(5,64);
    check(oSet, 50, "[15..64]");
    oSet.retainAll(15,74);
    check(oSet, 50, "[15..64]");
    oSet.retainAll(24,55);
    check(oSet, 32, "[24..55]");
    oSet.retainAll(getStandardSet2());
    check(oSet, 12, "[24,35..44,55]");
    oSet.invert();
    check(oSet, 2147483647, "[-2147483648..23,25..34,45..54,56..2147483647]");
    oSet.removeAll(999,1001);
    check(oSet, 2147483647, "[-2147483648..23,25..34,45..54,56..998,1002..2147483647]");

    oSet = getStandardSet3();
    check(oSet, 30, "[20..29,40..49,60..69]");
    oSet.retainAll(getStandardSet3());
    check(oSet, 30, "[20..29,40..49,60..69]");
    oSet.retainAll(getStandardSet2());
    check(oSet, 15, "[20..24,40..44,60..64]");
    oSet.retainAll(getStandardSet1());
    check(oSet, 0, "[]");
    oSet.addAll(getStandardSet2());
    check(oSet, 30, "[15..24,35..44,55..64]");
    oSet.addAll(getStandardSet3());
    check(oSet, 45, "[15..29,35..49,55..69]");
    oSet.addAll(getStandardSet3());
    check(oSet, 45, "[15..29,35..49,55..69]");
    oSet.addAll(getStandardSet2());
    check(oSet, 45, "[15..29,35..49,55..69]");
    assertFalse("The set should not have contained all elements but " +
                "indicated as such",
                oSet.containsAll(getStandardSet1()));
    check(oSet, 45, "[15..29,35..49,55..69]");
    assertTrue("The set should have contained all elements but did not " +
               "indicate as such",
               oSet.containsAll(getStandardSet2()));
    check(oSet, 45, "[15..29,35..49,55..69]");
    oSet.addAll(getStandardSet1());
    check(oSet, 60, "[10..69]");
    oSet.clear();
    check(oSet, 0, "[]");
    oSet.removeAll(getStandardSet3());
    check(oSet, 0, "[]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.addAll(20, 44);
    check(oSet, 45, "[10..44,50..59]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.addAll(25, 49);
    check(oSet, 45, "[10..19,25..59]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.removeAll(25, 44);
    check(oSet, 20, "[10..19,50..59]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.removeAll(15, 44);
    check(oSet, 15, "[10..14,50..59]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.removeAll(25, 54);
    check(oSet, 15, "[10..19,55..59]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.removeAll(30, 44);
    check(oSet, 20, "[10..19,50..59]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.removeAll(15, 39);
    check(oSet, 15, "[10..14,50..59]");

    oSet = getStandardSet1();
    check(oSet, 30, "[10..19,30..39,50..59]");
    oSet.removeAll(15, 54);
    check(oSet, 10, "[10..14,55..59]");
    assertTrue("Set did not indicate that it contains a value that is does.",
               oSet.contains(10));
    assertTrue("Set did not indicate that it contains a value that is does.",
               oSet.contains(13));
    assertTrue("Set did not indicate that it contains a value that is does.",
               oSet.contains(14));
    assertFalse("Set indicated that it contains a value that is does not.",
                oSet.contains(15));
    assertFalse("Set indicated that it contains a value that is does not.",
                oSet.contains(16));
    assertFalse("Set indicated that it contains a value that is does not.",
                oSet.contains(54));
    assertFalse("Set indicated that it contains a value that is does not.",
                oSet.contains(Integer.MIN_VALUE));
    assertFalse("Set indicated that it contains a value that is does not.",
                oSet.contains(Integer.MAX_VALUE));

    oSet = getStandardSet1();
    assertTrue("Set indicated that it does not contain values that it does",
               oSet.containsAny(getStandardSet2()));
    assertFalse("Set indicated that it contains values that it does not.",
               oSet.containsAny(getStandardSet3()));
  }

  @Test
  public void testIteratorFunctions ()
  {
    final TestSet oSet = getStandardSet1();

    new TestIterator<Iterator<Integer>>(oSet.iterator(),
                                        31,
                                        null,
                                        new long[]{10,11,12,13,14,15,16,17,18,19,
                                                   30,31,32,33,34,35,36,37,38,39,
                                                   50,51,52,53,54,55,56,57,58,59,
                                                   TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return _oTestObj.hasNext();
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        return _oTestObj.next();
      }
    }.test();

    new TestIterator<Iterator<Integer>>(oSet.iterator(40),
                                        11,
                                        null,
                                        new long[]{50,51,52,53,54,55,56,57,58,59,
                                                   TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return _oTestObj.hasNext();
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        return _oTestObj.next();
      }
    }.test();

    new TestIterator<Iterator<Integer>>(oSet.iterator(55),
                                        6,
                                        null,
                                        new long[]{55,56,57,58,59,
                                                   TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return _oTestObj.hasNext();
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        return _oTestObj.next();
      }
    }.test();

    oSet.addAll(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
    check(oSet, 32, "[-2147483648..-2147483647,10..19,30..39,50..59]");
    oSet.addAll(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    check(oSet, 34, "[-2147483648..-2147483647,10..19,30..39,50..59,2147483646..2147483647]");
    new TestIterator<Iterator<Integer>>(oSet.iterator(),
                                        35,
                                        null,
                                        new long[]{Integer.MIN_VALUE,
                                                   Integer.MIN_VALUE + 1,
                                                   10,11,12,13,14,15,16,17,18,19,
                                                   30,31,32,33,34,35,36,37,38,39,
                                                   50,51,52,53,54,55,56,57,58,59,
                                                   Integer.MAX_VALUE - 1,
                                                   Integer.MAX_VALUE,
                                                   TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return _oTestObj.hasNext();
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        return _oTestObj.next();
      }
    }.test();

    new TestIterator<Iterator<Integer>>(oSet.iterator(),
                                        13,
                                        null,
                                        new long[]{Integer.MIN_VALUE,
                                                   Integer.MIN_VALUE + 1,
                                                   10,11,12,13,14,15,16,17,
                                                   TEST_EXPECT_ILLEGAL_STATE_EXCEPTION,
                                                   19,
                                                   TEST_CONCURRENT_MODIFICATION_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return _oTestObj.hasNext();
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        Integer oiValue = _oTestObj.next();
        if (oiValue == 18)
        {
          _oTestObj.remove();
          _oTestObj.remove(); // throws ise
        }
        else if (oiValue == 19)
        {
          oSet.removeAll(30, 39);
        }
        return oiValue;
      }
    }.test();
    check(oSet, 23, "[-2147483648..-2147483647,10..17,19,50..59,2147483646..2147483647]");

    new TestIterator<Iterator<Integer>>(oSet.iterator(),
                                        24,
                                        null,
                                        new long[]{Integer.MIN_VALUE,
                                                   Integer.MIN_VALUE + 1,
                                                   10,11,12,13,14,15,16,17,19,
                                                   50,51,52,53,54,55,56,57,58,59,
                                                   Integer.MAX_VALUE - 1,
                                                   Integer.MAX_VALUE,
                                                   TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return _oTestObj.hasNext();
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        Integer oiValue = _oTestObj.next();
        if (oiValue == 19)
        {
          oSet.removeAll(30, 39);
        }
        return oiValue;
      }
    }.test();
    check(oSet, 23, "[-2147483648..-2147483647,10..17,19,50..59,2147483646..2147483647]");

    new TestIterator<Iterator<Integer>>(oSet.iterator(),
                                        24,
                                        null,
                                        new long[]{Integer.MIN_VALUE,
                                                   Integer.MIN_VALUE + 1,
                                                   10,11,12,13,14,15,16,17,19,
                                                   50,51,52,53,54,55,56,57,58,59,
                                                   Integer.MAX_VALUE - 1,
                                                   Integer.MAX_VALUE,
                                                   TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return _oTestObj.hasNext();
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        Integer oiValue = _oTestObj.next();
        _oTestObj.remove();
        return oiValue;
      }
    }.test();
    check(oSet, 0, "[]");
  }

  @Test
  public void testNextFunctions ()
  {
    final TestSet oSet = getStandardSet1();

    new TestIterator<TestSet>(oSet,
                              13,
                              new int[]{ 0, 5,10,15,20,25,30,35,40,45,50,55,60},
                              new long[]{10,10,10,15,30,30,30,35,50,50,50,55,
                                         TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return iSeq < 12;
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        return _oTestObj.getNext(iValue);
      }
    }.test();

    oSet.add(Integer.MAX_VALUE);
    new TestIterator<TestSet>(oSet,
                              13,
                              new int[]{ 0, 5,10,15,20,25,30,35,40,45,50,55,
                                        Integer.MAX_VALUE},
                              new long[]{0, 5,20,20,20,25,40,40,40,45,60,60,
                                         TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION})
    {
      protected boolean hasNext (int iSeq)
      {
        return iSeq < 12;
      }

      protected Integer next (int iValue) throws NoSuchElementException, IllegalStateException
      {
        return _oTestObj.getNextNotIncluded(iValue);
      }
    }.test();
  }

  @Test
  public void testUtilityFunctions ()
  {
    TestSet oSet = getStandardSet1();
    assertEquals("Same sets did not match", oSet, oSet);
    assertEquals("Identical empty sets did not match",
                 new TestSet(),
                 new TestSet());
    assertEquals("Identical sets did not match", oSet, getStandardSet1());
    assertEquals("Identical sets did not match", getStandardSet1(), oSet);
    assertEquals("Cloned sets did not match", oSet.clone(), oSet);
    assertEquals("Cloned sets did not match", oSet, oSet.clone());
    assertFalse("Non-identical sets indicated a match",
                getStandardSet2().equals(oSet));
    assertFalse("Non-identical sets indicated a match",
                oSet.equals(getStandardSet2()));
    assertFalse("Non-identical sets indicated a match",
                getStandardSet3().equals(oSet));
    assertFalse("Non-identical sets indicated a match",
                oSet.equals(getStandardSet3()));
    assertFalse("Non-identical sets indicated a match",
                new TestSet().equals(oSet));
    assertFalse("Non-identical sets indicated a match",
                oSet.equals(new TestSet()));
    assertFalse("Set matched NULL (WTF?)",
                oSet.equals(null));
    assertFalse("Set matched a garbage string (WTF?)",
                oSet.equals("Peanut butter and jelly sandwich"));

    assertEquals("Expected hashCode() was incorrect",
                 new TestSet().hashCode(),
                 30);
    assertEquals("Expected hashCode() was incorrect",
                 getStandardSet1().hashCode(),
                 75490015);
    assertEquals("Expected hashCode() was incorrect",
                 getStandardSet2().hashCode(),
                 656426783);
    assertEquals("Expected hashCode() was incorrect",
                 getStandardSet3().hashCode(),
                 1237363551);
  }

  @Test
  public void testPerformance ()
  {
//    final int iNumCycles = 100000; // use for a real performance test!
    final int iNumCycles = 1000;

    RangeBasedIntegerSet oSet = new RangeBasedIntegerSet();
    for (int i = 0; i < iNumCycles; i += 10)
    {
      oSet.add(i);
    }
    for (int i = 0; i < iNumCycles; i += 10)
    {
      oSet.add(i + 5);
    }
    for (int i = 0; i < iNumCycles; i += 10)
    {
      oSet.addAll(i, i + 5);
    }
    for (int i = 0; i < iNumCycles; i += 10)
    {
      oSet.remove(i);
    }
    for (int i = 0; i < iNumCycles; i += 10)
    {
      oSet.remove(i + 3);
    }
    oSet.removeAll(0, iNumCycles);
  }

  @Test
  public void testInvalidRanges ()
  {
    RangeBasedIntegerSet oSet = new RangeBasedIntegerSet();
    try
    {
      oSet.addAll(100, 0);
      assertTrue("A range operation involving a mis-ordered from/to pair was " +
                 "permitted",
                 false);
    }
    catch (IllegalArgumentException e)
    {
      // This is the expected result from this test case.
    }
  }

  private abstract static class TestIterator<TO>
  {
    protected final TO _oTestObj;
    private final int _iIterations;
    private final int[] _arTestData;
    private final long[] _arExpectedResults;

    TestIterator (TO oTestObj,
                  int iIterations,
                  int[] arTestData,
                  long[] arExpectedResults)
    {
      _oTestObj = oTestObj;
      _iIterations = iIterations;
      _arTestData = arTestData;
      _arExpectedResults = arExpectedResults;
    }

    protected boolean hasNext (int iSeq)
    {
      return true;
    }

    protected abstract Integer next (int iValue) throws NoSuchElementException,
                                                        IllegalStateException;

    final void test ()
    {
      for (int i = 0; i < _iIterations; i++)
      {
        int iValue = (_arTestData != null) ? _arTestData[i] : 0;
        int iResult;
        long lExpected = _arExpectedResults[i];
        boolean boHasNext;
        try
        {
          boHasNext = hasNext(i);
          if (lExpected < 3000000000L)
          {
            assertTrue("hasNext() did not report another value was available " +
                       "when one is expected [" + i + "]",
                       boHasNext);
          }
          else if (lExpected == TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION)
          {
            assertFalse("hasNext() reported another value was available " +
                        "when expecting no more elements",
                        boHasNext);
          }
          Integer oiResult = next(iValue);
          if (lExpected == TEST_EXPECT_NULL)
          {
            assertNull("Null was expected [" + i + "]", oiResult);
          }
          else if (lExpected == TEST_EXPECT_ILLEGAL_STATE_EXCEPTION)
          {
            assertFalse("Expected an IllegalStateException to be thrown " +
                        "[" + i + "]",
                        true);
          }
          else if (lExpected == TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION)
          {
            assertFalse("Expected a NoSuchElementException to be thrown " +
                        "[" + i + "]",
                        true);
          }
          else if (lExpected == TEST_CONCURRENT_MODIFICATION_EXCEPTION)
          {
            assertFalse("Expected a ConcurrentModificationException to be " +
                        "thrown [" + i + "]",
                        true);
          }
          else
          {
            iResult = oiResult.intValue();
            assertEquals("Value was not the expected value [" + i + "]",
                         (int)lExpected,
                         iResult);
          }
        }
        catch (NoSuchElementException nsee)
        {
          if (lExpected != TEST_EXPECT_NO_SUCH_ELEMENT_EXCEPTION)
          {
            throw nsee;
          }
        }
        catch (IllegalStateException ise)
        {
          if (lExpected != TEST_EXPECT_ILLEGAL_STATE_EXCEPTION)
          {
            throw ise;
          }
        }
        catch (ConcurrentModificationException ccme)
        {
          if (lExpected != TEST_CONCURRENT_MODIFICATION_EXCEPTION)
          {
            throw ccme;
          }
        }
      }
    }
  }

  // Test subclass that validates the collection after every operation that
  // possibly modifies it.
  private static class TestSet extends RangeBasedIntegerSet
  {

    public TestSet ()
    {
      super();
      assertOptimized();
    }

    public TestSet (RangeBasedIntegerSet oIntSet)
    {
      super(oIntSet);
      assertOptimized();
    }

    public boolean add (int iValue)
    {
      boolean boChanged = super.add(iValue);
      assertOptimized();
      return boChanged;
    }

    public boolean remove (int iValue)
    {
      boolean boChanged = super.remove(iValue);
      assertOptimized();
      return boChanged;
    }

    public boolean addAll (int iFrom, int iTo)
    {
      boolean boChanged = super.addAll(iFrom, iTo);
      assertOptimized();
      return boChanged;
    }

    public boolean addAll (RangeBasedIntegerSet oValues)
    {
      boolean boChanged = super.addAll(oValues);
      assertOptimized();
      return boChanged;
    }

    public boolean retainAll (int iFrom, int iTo)
    {
      boolean boChanged = super.retainAll(iFrom, iTo);
      assertOptimized();
      return boChanged;
    }

    public boolean retainAll (RangeBasedIntegerSet oValues)
    {
      boolean boChanged = super.retainAll(oValues);
      assertOptimized();
      return boChanged;
    }

    public boolean removeAll (int iFrom, int iTo)
    {
      boolean boChanged = super.removeAll(iFrom, iTo);
      assertOptimized();
      return boChanged;
    }

    public boolean removeAll (RangeBasedIntegerSet oValues)
    {
      boolean boChanged = super.removeAll(oValues);
      assertOptimized();
      return boChanged;
    }

    public Iterator<Integer> iterator (int iFirstValue)
    {
      final Iterator<Integer> iterator = super.iterator(iFirstValue);
      return new Iterator<Integer>()
      {
        public boolean hasNext ()
        {
          return iterator.hasNext();
        }

        public Integer next ()
        {
          return iterator.next();
        }

        public void remove ()
        {
          iterator.remove();
          assertOptimized();
        }
      };
    }

    public void invert ()
    {
      super.invert();
      assertOptimized();
    }

    public void clear ()
    {
      super.clear();
    }

    public void assertOptimized ()
    {
      assertNotNull("_liAllRanges should not be null", _liAllRanges);
      assertTrue("_liAllRanges should never be empty",
                          _liAllRanges.size() > 0);
      Range oLeftRange = _liAllRanges.get(0);
      assertEquals("The first range should always begin with MIN_VALUE",
                            Integer.MIN_VALUE, oLeftRange.getBegin());
      Range oRightRange;
      long lSize = oLeftRange.size();
      for (int i = 1; i < _liAllRanges.size(); i++)
      {
        oRightRange = _liAllRanges.get(i);
        assertEquals("Ranges should always start immediately after " +
                     "the preceeding range [" + i + "]",
                     oLeftRange.getEnd() + 1,
                     oRightRange.getBegin());
        assertTrue("Consecutive ranges should not have the same " +
                   "inclusion [" + i + "]",
                   oLeftRange.isIncluded() != oRightRange.isIncluded());
        assertTrue("Ranges should always end on or after they begin " +
                   "[" + i + "]",
                   oRightRange.getBegin() <= oRightRange.getEnd());
        lSize += oRightRange.size();
        oLeftRange = oRightRange;
      }
      assertEquals("The last range should always end with MAX_VALUE",
                   Integer.MAX_VALUE, oLeftRange.getEnd());
      assertEquals("This set does not indicate the correct size",
                   lSize,
                   _lSize);
    }
  }
}
